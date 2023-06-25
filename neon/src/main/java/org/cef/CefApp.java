// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package org.cef;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefAppHandler;
import org.cef.handler.CefAppHandlerAdapter;

/** Exposes static methods for managing the global CEF context. */
public class CefApp extends CefAppHandlerAdapter {
  public final class CefVersion {
    public final int JCEF_COMMIT_NUMBER;

    public final int CEF_VERSION_MAJOR;
    public final int CEF_VERSION_MINOR;
    public final int CEF_VERSION_PATCH;
    public final int CEF_COMMIT_NUMBER;

    public final int CHROME_VERSION_MAJOR;
    public final int CHROME_VERSION_MINOR;
    public final int CHROME_VERSION_BUILD;
    public final int CHROME_VERSION_PATCH;

    private CefVersion(
        final int jcefCommitNo,
        final int cefMajor,
        final int cefMinor,
        final int cefPatch,
        final int cefCommitNo,
        final int chrMajor,
        final int chrMin,
        final int chrBuild,
        final int chrPatch) {
      this.JCEF_COMMIT_NUMBER = jcefCommitNo;

      this.CEF_VERSION_MAJOR = cefMajor;
      this.CEF_VERSION_MINOR = cefMinor;
      this.CEF_VERSION_PATCH = cefPatch;
      this.CEF_COMMIT_NUMBER = cefCommitNo;

      this.CHROME_VERSION_MAJOR = chrMajor;
      this.CHROME_VERSION_MINOR = chrMin;
      this.CHROME_VERSION_BUILD = chrBuild;
      this.CHROME_VERSION_PATCH = chrPatch;
    }

    public String getJcefVersion() {
      return this.CEF_VERSION_MAJOR
          + "."
          + this.CEF_VERSION_MINOR
          + "."
          + this.CEF_VERSION_PATCH
          + "."
          + this.JCEF_COMMIT_NUMBER;
    }

    public String getCefVersion() {
      return this.CEF_VERSION_MAJOR + "." + this.CEF_VERSION_MINOR + "." + this.CEF_VERSION_PATCH;
    }

    public String getChromeVersion() {
      return this.CHROME_VERSION_MAJOR
          + "."
          + this.CHROME_VERSION_MINOR
          + "."
          + this.CHROME_VERSION_BUILD
          + "."
          + this.CHROME_VERSION_PATCH;
    }

    @Override
    public String toString() {
      return "JCEF Version = "
          + this.getJcefVersion()
          + "\n"
          + "CEF Version = "
          + this.getCefVersion()
          + "\n"
          + "Chromium Version = "
          + this.getChromeVersion();
    }
  }

  /**
   * The CefAppState gives you a hint if the CefApp is already usable or not usable any more. See
   * values for details.
   */
  public enum CefAppState {
    /** No CefApp instance was created yet. Call getInstance() to create a new one. */
    NONE,

    /**
     * CefApp is new created but not initialized yet. No CefClient and no CefBrowser was created
     * until now.
     */
    NEW,

    /** CefApp is in its initializing process. Please wait until initializing is finished. */
    INITIALIZING,

    /**
     * CefApp is up and running. At least one CefClient was created and the message loop is running.
     * You can use all classes and methods of JCEF now.
     */
    INITIALIZED,

    /**
     * CefApp is in its shutdown process. All CefClients and CefBrowser instances will be disposed.
     * No new CefClient or CefBrowser is allowed to be created. The message loop will be performed
     * until all CefClients and all CefBrowsers are disposed completely.
     */
    SHUTTING_DOWN,

    /**
     * CefApp is terminated and can't be used any more. You can shutdown the application safely now.
     */
    TERMINATED
  }

  /** According the singleton pattern, this attribute keeps one single object of this class. */
  private static CefApp self = null;

  private static CefAppHandler appHandler_ = null;
  private static CefAppState state_ = CefAppState.NONE;
  private Timer workTimer_ = null;
  private final HashSet<CefClient> clients_ = new HashSet<CefClient>();
  private CefSettings settings_ = null;

  /**
   * To get an instance of this class, use the method getInstance() instead of this CTOR.
   *
   * <p>The CTOR is called by getInstance() as needed and loads all required JCEF libraries.
   *
   * @throws UnsatisfiedLinkError
   */
  private CefApp(final String[] args, final CefSettings settings) throws UnsatisfiedLinkError {
    super(args);
    if (settings != null) {
      this.settings_ = settings.clone();
    }
    if (OS.isWindows()) {
      SystemBootstrap.loadLibrary("jawt");
      SystemBootstrap.loadLibrary("chrome_elf");
      SystemBootstrap.loadLibrary("libcef");

      // Other platforms load this library in CefApp.startup().
      SystemBootstrap.loadLibrary("jcef");
    } else if (OS.isLinux()) {
      SystemBootstrap.loadLibrary("cef");
    }
    if (appHandler_ == null) {
      appHandler_ = this;
    }

    // Execute on the AWT event dispatching thread.
    try {
      final Runnable r =
          new Runnable() {
            @Override
            public void run() {
              // Perform native pre-initialization.
              if (!CefApp.this.N_PreInitialize()) {
                throw new IllegalStateException("Failed to pre-initialize native code");
              }
            }
          };
      if (SwingUtilities.isEventDispatchThread()) {
        r.run();
      } else {
        SwingUtilities.invokeAndWait(r);
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Assign an AppHandler to CefApp. The AppHandler can be used to evaluate application arguments,
   * to register your own schemes and to hook into the shutdown sequence. See CefAppHandler for more
   * details.
   *
   * <p>This method must be called before CefApp is initialized. CefApp will be initialized
   * automatically if you call createClient() the first time.
   *
   * @param appHandler An instance of CefAppHandler.
   * @throws IllegalStateException in case of CefApp is already initialized
   */
  public static void addAppHandler(final CefAppHandler appHandler) throws IllegalStateException {
    if (getState().compareTo(CefAppState.NEW) > 0) {
      throw new IllegalStateException("Must be called before CefApp is initialized");
    }
    appHandler_ = appHandler;
  }

  /**
   * Get an instance of this class.
   *
   * @return an instance of this class
   * @throws UnsatisfiedLinkError
   */
  public static synchronized CefApp getInstance() throws UnsatisfiedLinkError {
    return getInstance(null, null);
  }

  public static synchronized CefApp getInstance(final String[] args) throws UnsatisfiedLinkError {
    return getInstance(args, null);
  }

  public static synchronized CefApp getInstance(final CefSettings settings)
      throws UnsatisfiedLinkError {
    return getInstance(null, settings);
  }

  public static synchronized CefApp getInstance(final String[] args, final CefSettings settings)
      throws UnsatisfiedLinkError {
    if (settings != null) {
      if (getState() != CefAppState.NONE && getState() != CefAppState.NEW) {
        throw new IllegalStateException(
            "Settings can only be passed to CEF"
                + " before createClient is called the first time.");
      }
    }
    if (self == null) {
      if (getState() == CefAppState.TERMINATED) {
        throw new IllegalStateException("CefApp was terminated");
      }
      self = new CefApp(args, settings);
      setState(CefAppState.NEW);
    }
    return self;
  }

  public final void setSettings(final CefSettings settings) throws IllegalStateException {
    if (getState() != CefAppState.NONE && getState() != CefAppState.NEW) {
      throw new IllegalStateException(
          "Settings can only be passed to CEF" + " before createClient is called the first time.");
    }
    this.settings_ = settings.clone();
  }

  public final CefVersion getVersion() {
    try {
      return this.N_GetVersion();
    } catch (final UnsatisfiedLinkError ule) {
      ule.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the current state of CefApp.
   *
   * @return current state.
   */
  public static final CefAppState getState() {
    synchronized (state_) {
      return state_;
    }
  }

  private static final void setState(final CefAppState state) {
    synchronized (state_) {
      state_ = state;
    }
    // Execute on the AWT event dispatching thread.
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            if (appHandler_ != null) {
              appHandler_.stateHasChanged(state);
            }
          }
        });
  }

  /**
   * To shutdown the system, it's important to call the dispose method. Calling this method closes
   * all client instances with and all browser instances each client owns. After that the message
   * loop is terminated and CEF is shutdown.
   */
  public final synchronized void dispose() {
    switch (getState()) {
      case NEW:
        // Nothing to do inspite of invalidating the state
        setState(CefAppState.TERMINATED);
        break;

      case INITIALIZING:
      case INITIALIZED:
        // (3) Shutdown sequence. Close all clients and continue.
        setState(CefAppState.SHUTTING_DOWN);
        if (this.clients_.isEmpty()) {
          this.shutdown();
        } else {
          // shutdown() will be called from clientWasDisposed() when the last
          // client is gone.
          // Use a copy of the HashSet to avoid iterating during modification.
          final HashSet<CefClient> clients = new HashSet<CefClient>(this.clients_);
          for (final CefClient c : clients) {
            c.dispose();
          }
        }
        break;

      case NONE:
      case SHUTTING_DOWN:
      case TERMINATED:
        // Ignore shutdown, CefApp is already terminated, in shutdown progress
        // or was never created (shouldn't be possible)
        break;
    }
  }

  /**
   * Creates a new client instance and returns it to the caller. One client instance is responsible
   * for one to many browser instances
   *
   * @return a new client instance
   */
  public synchronized CefClient createClient() {
    switch (getState()) {
      case NEW:
        setState(CefAppState.INITIALIZING);
        this.initialize();
        // FALL THRU

      case INITIALIZING:
      case INITIALIZED:
        final CefClient client = new CefClient();
        this.clients_.add(client);
        return client;

      default:
        throw new IllegalStateException("Can't crate client in state " + state_);
    }
  }

  /**
   * Register a scheme handler factory for the specified |scheme_name| and optional |domain_name|.
   * An empty |domain_name| value for a standard scheme will cause the factory to match all domain
   * names. The |domain_name| value will be ignored for non-standard schemes. If |scheme_name| is a
   * built-in scheme and no handler is returned by |factory| then the built-in scheme handler
   * factory will be called. If |scheme_name| is a custom scheme then also implement the
   * CefApp::OnRegisterCustomSchemes() method in all processes. This function may be called multiple
   * times to change or remove the factory that matches the specified |scheme_name| and optional
   * |domain_name|. Returns false if an error occurs. This function may be called on any thread in
   * the browser process.
   */
  public boolean registerSchemeHandlerFactory(
      final String schemeName, final String domainName, final CefSchemeHandlerFactory factory) {
    try {
      return this.N_RegisterSchemeHandlerFactory(schemeName, domainName, factory);
    } catch (final Exception err) {
      err.printStackTrace();
    }
    return false;
  }

  /**
   * Clear all registered scheme handler factories. Returns false on error. This function may be
   * called on any thread in the browser process.
   */
  public boolean clearSchemeHandlerFactories() {
    try {
      return this.N_ClearSchemeHandlerFactories();
    } catch (final Exception err) {
      err.printStackTrace();
    }
    return false;
  }

  /**
   * This method is called by a CefClient if it was disposed. This causes CefApp to clean up its
   * list of available client instances. If all clients are disposed, CefApp will be shutdown.
   *
   * @param client the disposed client.
   */
  protected final synchronized void clientWasDisposed(final CefClient client) {
    this.clients_.remove(client);
    if (this.clients_.isEmpty() && getState().compareTo(CefAppState.SHUTTING_DOWN) >= 0) {
      // Shutdown native system.
      this.shutdown();
    }
  }

  /**
   * Initialize the context.
   *
   * @return true on success.
   */
  private final void initialize() {
    // Execute on the AWT event dispatching thread.
    try {
      final Runnable r =
          new Runnable() {
            @Override
            public void run() {
              final String library_path = getJcefLibPath();
              final CefSettings settings =
                  CefApp.this.settings_ != null ? CefApp.this.settings_ : new CefSettings();

              // Avoid to override user values by testing on NULL
              if (OS.isMacintosh()) {
                if (settings.browser_subprocess_path == null) {
                  final Path path =
                      Paths.get(
                          library_path, "../Frameworks/jcef Helper.app/Contents/MacOS/jcef Helper");
                  settings.browser_subprocess_path = path.normalize().toAbsolutePath().toString();
                }
              } else if (OS.isWindows()) {
                if (settings.browser_subprocess_path == null) {
                  final Path path = Paths.get(library_path, "jcef_helper.exe");
                  settings.browser_subprocess_path = path.normalize().toAbsolutePath().toString();
                }
              } else if (OS.isLinux()) {
                if (settings.browser_subprocess_path == null) {
                  final Path path = Paths.get(library_path, "jcef_helper");
                  settings.browser_subprocess_path = path.normalize().toAbsolutePath().toString();
                }
                if (settings.resources_dir_path == null) {
                  final Path path = Paths.get(library_path);
                  settings.resources_dir_path = path.normalize().toAbsolutePath().toString();
                }
                if (settings.locales_dir_path == null) {
                  final Path path = Paths.get(library_path, "locales");
                  settings.locales_dir_path = path.normalize().toAbsolutePath().toString();
                }
              }
              if (CefApp.this.N_Initialize(appHandler_, settings)) {
                setState(CefAppState.INITIALIZED);
              }
            }
          };
      if (SwingUtilities.isEventDispatchThread()) {
        r.run();
      } else {
        SwingUtilities.invokeAndWait(r);
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is invoked by the native code (currently on Mac only) in case of a termination
   * event (e.g. someone pressed CMD+Q).
   */
  protected final void handleBeforeTerminate() {
    // Execute on the AWT event dispatching thread. Always call asynchronously
    // so the call stack has a chance to unwind.
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            final CefAppHandler handler =
                (CefAppHandler) ((appHandler_ == null) ? this : appHandler_);
            if (!handler.onBeforeTerminate()) {
              CefApp.this.dispose();
            }
          }
        });
  }

  /** Shut down the context. */
  private final void shutdown() {
    // Execute on the AWT event dispatching thread. Always call asynchronously
    // so the call stack has a chance to unwind.
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            // Shutdown native CEF.
            CefApp.this.N_Shutdown();

            setState(CefAppState.TERMINATED);
            CefApp.self = null;
          }
        });
  }

  /**
   * Perform a single message loop iteration. Used on all platforms except Windows with windowed
   * rendering.
   */
  public final void doMessageLoopWork(final long delay_ms) {
    // Execute on the AWT event dispatching thread.
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            if (getState() == CefAppState.TERMINATED) {
              return;
            }

            // The maximum number of milliseconds we're willing to wait between
            // calls to DoMessageLoopWork().
            final long kMaxTimerDelay = 1000 / 30; // 30fps

            if (CefApp.this.workTimer_ != null) {
              CefApp.this.workTimer_.stop();
              CefApp.this.workTimer_ = null;
            }

            if (delay_ms <= 0) {
              // Execute the work immediately.
              CefApp.this.N_DoMessageLoopWork();

              // Schedule more work later.
              CefApp.this.doMessageLoopWork(kMaxTimerDelay);
            } else {
              long timer_delay_ms = delay_ms;
              // Never wait longer than the maximum allowed time.
              if (timer_delay_ms > kMaxTimerDelay) {
                timer_delay_ms = kMaxTimerDelay;
              }

              CefApp.this.workTimer_ =
                  new Timer(
                      (int) timer_delay_ms,
                      new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent evt) {
                          // Timer has timed out.
                          CefApp.this.workTimer_.stop();
                          CefApp.this.workTimer_ = null;

                          CefApp.this.N_DoMessageLoopWork();

                          // Schedule more work later.
                          CefApp.this.doMessageLoopWork(kMaxTimerDelay);
                        }
                      });
              CefApp.this.workTimer_.start();
            }
          }
        });
  }

  /**
   * This method must be called at the beginning of the main() method to perform platform- specific
   * startup initialization. On Linux this initializes Xlib multithreading and on macOS this
   * dynamically loads the CEF framework.
   *
   * @param args Command-line arguments massed to main().
   * @return True on successful startup.
   */
  public static final boolean startup(final String[] args) {
    if (OS.isLinux() || OS.isMacintosh()) {
      SystemBootstrap.loadLibrary("jcef");
      return N_Startup(OS.isMacintosh() ? getCefFrameworkPath(args) : null);
    }
    return true;
  }

  /**
   * Get the path which contains the jcef library
   *
   * @return The path to the jcef library
   */
  private static final String getJcefLibPath() {
    final String library_path = System.getProperty("java.library.path");
    final String[] paths = library_path.split(System.getProperty("path.separator"));
    for (final String path : paths) {
      final File dir = new File(path);
      final String[] found =
          dir.list(
              new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                  return (name.equalsIgnoreCase("libjcef.dylib")
                      || name.equalsIgnoreCase("libjcef.so")
                      || name.equalsIgnoreCase("jcef.dll"));
                }
              });
      if (found != null && found.length != 0) {
        return path;
      }
    }
    return library_path;
  }

  /**
   * Get the path that contains the CEF Framework on macOS.
   *
   * @return The path to the CEF Framework.
   */
  private static final String getCefFrameworkPath(final String[] args) {
    // Check for the path on the command-line.
    final String switchPrefix = "--framework-dir-path=";
    for (final String arg : args) {
      if (arg.startsWith(switchPrefix)) {
        return new File(arg.substring(switchPrefix.length())).getAbsolutePath();
      }
    }

    // Determine the path relative to the JCEF lib location in the app bundle.
    return new File(getJcefLibPath() + "/../Frameworks/Chromium Embedded Framework.framework")
        .getAbsolutePath();
  }

  private static final native boolean N_Startup(String pathToCefFramework);

  private final native boolean N_PreInitialize();

  private final native boolean N_Initialize(CefAppHandler appHandler, CefSettings settings);

  private final native void N_Shutdown();

  private final native void N_DoMessageLoopWork();

  private final native CefVersion N_GetVersion();

  private final native boolean N_RegisterSchemeHandlerFactory(
      String schemeName, String domainName, CefSchemeHandlerFactory factory);

  private final native boolean N_ClearSchemeHandlerFactories();
}
