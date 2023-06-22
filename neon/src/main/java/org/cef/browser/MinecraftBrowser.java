package org.cef.browser;

import static java.util.Objects.requireNonNull;

import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.CefProgressHandler;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.OS;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MinecraftBrowser extends CefBrowser_N implements CefRenderHandler {
  private static @NotNull final CefClient CEF_CLIENT;

  static {
    try {
      final CefApp CEF_APP = createApp();
      CEF_CLIENT = CEF_APP.createClient();
    } catch (final UnsupportedPlatformException
        | CefInitializationException
        | IOException
        | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private @NotNull final Neon neon;
  private @NotNull final CefRenderer nativeRenderer;
  private @NotNull final BrowserSettings settings;
  private @NotNull final RenderMethod method;
  private @NotNull final Rectangle viewArea;
  private @Nullable GLCanvas canvas;
  private @Nullable CefRequestContext context;
  private @Nullable CefBrowser_N parent;
  private @NotNull Point screenPoint;
  private final @NotNull Rectangle browserRect;
  private int depth;
  private int componentDepth;
  private double scaleFactor;
  private long windowHandle;
  private boolean justCreated;

  public MinecraftBrowser(
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url) {
    this(CEF_CLIENT, neon, settings, method, url, null, null, null);
  }

  private MinecraftBrowser(
      @NotNull final CefClient client,
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url,
      @Nullable final CefRequestContext context,
      @Nullable final CefBrowser_N parent,
      @Nullable final Point inspectAt) {
    super(client, url, context, parent, inspectAt);
    this.context = context;
    this.parent = parent;
    this.neon = neon;
    this.settings = settings;
    this.method = method;
    this.justCreated = false;
    this.nativeRenderer = new CefRenderer(true);
    this.viewArea = this.getViewArea();
    this.depth = 32;
    this.componentDepth = 8;
    this.scaleFactor = 1.0;
    this.browserRect = new Rectangle(0, 0, 1, 1);
    this.screenPoint = new Point(0, 0);
    this.windowHandle = 0;
    this.createGLCanvas();
  }

  private void createGLCanvas() {
    final GLProfile glprofile = GLProfile.getMaxFixedFunc(true);
    final GLCapabilities glcapabilities = new GLCapabilities(glprofile);
    this.canvas =
        new GLCanvas(glcapabilities) {
          private boolean removed_ = true;

          @Override
          public void paint(final Graphics g) {
            MinecraftBrowser.this.createBrowserIfRequired(true);
            if (g instanceof Graphics2D) {
              final GraphicsConfiguration config = ((Graphics2D) g).getDeviceConfiguration();
              MinecraftBrowser.this.depth = config.getColorModel().getPixelSize();
              MinecraftBrowser.this.componentDepth = config.getColorModel().getComponentSize()[0];
              MinecraftBrowser.this.scaleFactor = ((Graphics2D) g).getTransform().getScaleX();
            }
            super.paint(g);
          }

          @Override
          public void addNotify() {
            super.addNotify();
            if (this.removed_) {
              MinecraftBrowser.this.notifyAfterParentChanged();
              this.removed_ = false;
            }
          }

          @Override
          public void removeNotify() {
            if (!this.removed_) {
              if (!MinecraftBrowser.this.isClosed()) {
                MinecraftBrowser.this.notifyAfterParentChanged();
              }
              this.removed_ = true;
            }
            super.removeNotify();
          }
        };

    this.canvas.addGLEventListener(
        new GLEventListener() {
          @Override
          public void reshape(
              final GLAutoDrawable glautodrawable,
              final int x,
              final int y,
              final int width,
              final int height) {
            int newWidth = width;
            int newHeight = height;
            if (OS.isMacintosh()) {
              newWidth = (int) (width / MinecraftBrowser.this.scaleFactor);
              newHeight = (int) (height / MinecraftBrowser.this.scaleFactor);
            }
            MinecraftBrowser.this.browserRect.setBounds(x, y, newWidth, newHeight);
            MinecraftBrowser.this.screenPoint = MinecraftBrowser.this.canvas.getLocationOnScreen();
            MinecraftBrowser.this.wasResized(newWidth, newHeight);
          }

          @Override
          public void init(final GLAutoDrawable glautodrawable) {
            MinecraftBrowser.this.nativeRenderer.initialize(glautodrawable.getGL().getGL2());
          }

          @Override
          public void dispose(final GLAutoDrawable glautodrawable) {
            MinecraftBrowser.this.nativeRenderer.cleanup(glautodrawable.getGL().getGL2());
          }

          @Override
          public void display(final GLAutoDrawable glautodrawable) {
            MinecraftBrowser.this.nativeRenderer.render(glautodrawable.getGL().getGL2());
          }
        });

    this.canvas.addMouseListener(
        new MouseListener() {
          @Override
          public void mousePressed(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }

          @Override
          public void mouseReleased(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }

          @Override
          public void mouseEntered(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }

          @Override
          public void mouseExited(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }

          @Override
          public void mouseClicked(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }
        });

    this.canvas.addMouseMotionListener(
        new MouseMotionListener() {
          @Override
          public void mouseMoved(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }

          @Override
          public void mouseDragged(final MouseEvent e) {
            MinecraftBrowser.this.sendMouseEvent(e);
          }
        });

    this.canvas.addMouseWheelListener(MinecraftBrowser.this::sendMouseWheelEvent);

    this.canvas.addKeyListener(
        new KeyListener() {
          @Override
          public void keyTyped(final KeyEvent e) {
            MinecraftBrowser.this.sendKeyEvent(e);
          }

          @Override
          public void keyPressed(final KeyEvent e) {
            MinecraftBrowser.this.sendKeyEvent(e);
          }

          @Override
          public void keyReleased(final KeyEvent e) {
            MinecraftBrowser.this.sendKeyEvent(e);
          }
        });

    this.canvas.setFocusable(true);
    this.canvas.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(final FocusEvent e) {
            MinecraftBrowser.this.setFocus(false);
          }

          @Override
          public void focusGained(final FocusEvent e) {
            MenuSelectionManager.defaultManager().clearSelectedPath();
            MinecraftBrowser.this.setFocus(true);
          }
        });
    new DropTarget(this.canvas, new CefDropTargetListener(this));
  }

  private void notifyAfterParentChanged() {
    this.getClient().onAfterParentChanged(this);
  }

  private @NotNull Rectangle getViewArea() {
    final ImmutableDimension dimension = this.settings.getResolution();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    return new Rectangle(0, 0, width, height);
  }

  public static void init() {}

  private static @NotNull CefApp createApp()
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    final CefSettings settings = new CefSettings();
    settings.log_severity = LogSeverity.LOGSEVERITY_WARNING;
    final CefAppBuilder builder = new CefAppBuilder();
    final Plugin plugin = requireNonNull(Bukkit.getPluginManager().getPlugin("Neon"));
    builder.setProgressHandler(new CefProgressHandler((Neon) plugin));
    builder.addJcefArgs("--disable-software-rasterizer");
    builder.addJcefArgs("--no-sandbox");
    builder.addJcefArgs("--disable-gpu");
    builder.addJcefArgs("--disable-gpu-compositing");
    builder.addJcefArgs("--log-level=1");
    final CefApp app = builder.build();
    app.setSettings(settings);
    return app;
  }

  private void createBrowserIfRequired(final boolean hasParent) {
    long windowHandle = 0;
    if (hasParent) {
      windowHandle = this.getWindowHandle();
    }
    if (this.getNativeRef("CefBrowser") == 0) {
      final CefBrowser_N parent = this.getParentBrowser();
      final CefClient client = this.getClient();
      final CefRequestContext context = this.getRequestContext();
      final Point inspect = this.getInspectAt();
      final String url = this.getUrl();
      if (this.getParentBrowser() != null) {
        this.createDevTools(parent, client, windowHandle, true, true, null, inspect);
      } else {
        this.createBrowser(client, windowHandle, url, true, true, null, context);
      }
    } else if (hasParent && this.justCreated) {
      this.notifyAfterParentChanged();
      this.setFocus(true);
      this.justCreated = false;
    }
  }

  private long getWindowHandle() {
    if (this.windowHandle == 0) {
      final NativeSurface surface = this.canvas.getNativeSurface();
      if (surface != null) {
        surface.lockSurface();
        this.windowHandle = this.getWindowHandle(surface.getSurfaceHandle());
        surface.unlockSurface();
      }
    }
    return this.windowHandle;
  }

  @Override
  public void close(final boolean force) {
    if (this.context != null) {
      this.context.dispose();
      this.context = null;
    }
    if (this.parent != null) {
      this.parent.closeDevTools();
      this.parent = null;
    }
    if (this.canvas != null) {
      this.canvas.setEnabled(false);
      this.canvas.destroy();
      this.canvas = null;
    }
    super.close(force);
  }

  @Override
  public CompletableFuture<BufferedImage> createScreenshot(final boolean nativeResolution) {
    throw new UnsupportedOperationException("Screenshot not supported!");
  }

  @Override
  public void createImmediately() {
    this.justCreated = true;
    this.createBrowserIfRequired(false);
  }

  @Override
  public Component getUIComponent() {
    return this.canvas;
  }

  @Override
  public CefBrowser_N createDevToolsBrowser(
      final CefClient client,
      final String url,
      final CefRequestContext context,
      final CefBrowser_N parent,
      final Point inspectAt) {
    return new MinecraftBrowser(
        client, this.neon, this.settings, this.method, url, context, parent, inspectAt);
  }

  @Override
  public CefRenderHandler getRenderHandler() {
    return this;
  }

  @Override
  public void onPaint(
      @NotNull final CefBrowser browser,
      final boolean popup,
      @NotNull final Rectangle[] dirtyRects,
      @NotNull final ByteBuffer buffer,
      final int width,
      final int height) {

    final GLContext context = this.canvas != null ? this.canvas.getContext() : null;
    if (context == null) {
      return;
    }

    if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
      return;
    }

    final GL2 gl2 = this.canvas.getGL().getGL2();
    this.nativeRenderer.onPaint(gl2, popup, dirtyRects, buffer, width, height);
    context.release();
    SwingUtilities.invokeLater(() -> this.canvas.display());

    final IntBuffer rgba = this.retrieveFrame(gl2, width, height);
    this.method.render(rgba);
  }

  public IntBuffer retrieveFrame(@NotNull final GL2 gl2, final int width, final int height) {

    final int textureId = this.nativeRenderer.getTextureID();
    final boolean useReadPixels = (textureId == 0);
    final ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
    buffer.order(ByteOrder.nativeOrder());
    gl2.getContext().makeCurrent();
    try {
      if (useReadPixels) {
        gl2.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
      } else {
        gl2.glEnable(GL.GL_TEXTURE_2D);
        gl2.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl2.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
        gl2.glDisable(GL.GL_TEXTURE_2D);
      }
    } finally {
      gl2.getContext().release();
    }

    final IntBuffer raster = IntBuffer.allocate(width * height);
    for (int i = 0; i < raster.capacity(); i++) {
      final int r = (buffer.get() & 0xff);
      final int g = (buffer.get() & 0xff);
      final int b = (buffer.get() & 0xff);
      final int a = (buffer.get() & 0xff);
      final int c = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
      raster.put(i, c);
    }

    return raster;
  }

  @Override
  public @NotNull Rectangle getViewRect(@NotNull final CefBrowser browser) {
    return this.viewArea;
  }

  @Override
  public boolean getScreenInfo(
      @NotNull final CefBrowser browser, @NotNull final CefScreenInfo screenInfo) {
    final Rectangle bounds = this.browserRect.getBounds();
    screenInfo.Set(this.scaleFactor, this.depth, this.componentDepth, false, bounds, bounds);
    return true;
  }

  @Override
  public @NotNull Point getScreenPoint(
      @NotNull final CefBrowser browser, @NotNull final Point viewPoint) {
    final Point point = new Point(this.screenPoint);
    point.translate(viewPoint.x, viewPoint.y);
    return point;
  }

  @Override
  public void onPopupShow(@NotNull final CefBrowser browser, final boolean show) {
    if (!show) {
      this.nativeRenderer.clearPopupRects();
      this.parent.invalidate();
    }
  }

  @Override
  public void onPopupSize(@NotNull final CefBrowser browser, @NotNull final Rectangle size) {
    this.nativeRenderer.onPopupSize(size);
  }

  @Override
  public boolean onCursorChange(@NotNull final CefBrowser browser, final int cursorType) {
    SwingUtilities.invokeLater(() -> this.canvas.setCursor(new Cursor(cursorType)));
    return true;
  }

  @Override
  public void updateDragCursor(final CefBrowser browser, final int operation) {}

  private static int getDndAction(final int mask) {
    int action = DnDConstants.ACTION_NONE;
    if ((mask & CefDragData.DragOperations.DRAG_OPERATION_COPY)
        == CefDragData.DragOperations.DRAG_OPERATION_COPY) {
      action = DnDConstants.ACTION_COPY;
    } else if ((mask & CefDragData.DragOperations.DRAG_OPERATION_MOVE)
        == CefDragData.DragOperations.DRAG_OPERATION_MOVE) {
      action = DnDConstants.ACTION_MOVE;
    } else if ((mask & CefDragData.DragOperations.DRAG_OPERATION_LINK)
        == CefDragData.DragOperations.DRAG_OPERATION_LINK) {
      action = DnDConstants.ACTION_LINK;
    }
    return action;
  }

  private static final class SyntheticDragGestureRecognizer extends DragGestureRecognizer {
    public SyntheticDragGestureRecognizer(
        final Component c, final int action, final MouseEvent triggerEvent) {
      super(new DragSource(), c, action);
      this.appendEvent(triggerEvent);
    }

    @Override
    protected void registerListeners() {}

    @Override
    protected void unregisterListeners() {}
  }

  @Override
  public boolean startDragging(
      final CefBrowser browser,
      final CefDragData dragData,
      final int mask,
      final int x,
      final int y) {
    final int action = getDndAction(mask);
    final MouseEvent triggerEvent =
        new MouseEvent(this.canvas, MouseEvent.MOUSE_DRAGGED, 0, 0, x, y, 0, false);
    final DragGestureEvent ev =
        new DragGestureEvent(
            new SyntheticDragGestureRecognizer(this.canvas, action, triggerEvent),
            action,
            new Point(x, y),
            new ArrayList<>(Arrays.asList(triggerEvent)));
    DragSource.getDefaultDragSource()
        .startDrag(
            ev,
            /* dragCursor= */ null,
            new StringSelection(dragData.getFragmentText()),
            new DragSourceAdapter() {
              @Override
              public void dragDropEnd(final DragSourceDropEvent dsde) {
                MinecraftBrowser.this.dragSourceEndedAt(dsde.getLocation(), action);
                MinecraftBrowser.this.dragSourceSystemDragEnded();
              }
            });
    return true;
  }

  public void sendNativeMouseEvent(@NotNull final MouseEvent event) {
    this.sendMouseEvent(event);
  }

  public void sendNativeKeyEvent(@NotNull final KeyEvent event) {
    this.sendKeyEvent(event);
  }

  public @NotNull Neon getNeon() {
    return this.neon;
  }

  public @NotNull BrowserSettings getSettings() {
    return this.settings;
  }

  public @NotNull RenderMethod getRenderMethod() {
    return this.method;
  }
}
