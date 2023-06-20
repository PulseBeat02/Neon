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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
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

  public MinecraftBrowser(
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url) {
    this(neon, settings, method, url, null, null, null);
  }

  private MinecraftBrowser(
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url,
      @Nullable final CefRequestContext context,
      @Nullable final CefBrowser_N parent,
      @Nullable final Point inspectAt) {
    super(CEF_CLIENT, url, context, parent, inspectAt);
    this.context = context;
    this.parent = parent;
    this.neon = neon;
    this.settings = settings;
    this.method = method;
    this.nativeRenderer = new CefRenderer(true);
    this.viewArea = this.getViewArea();
    this.canvas = this.createCanvas();
    this.depth = 32;
    this.componentDepth = 8;
    this.scaleFactor = 1.0;
    this.browserRect = new Rectangle(0, 0, 1, 1);
    this.screenPoint = new Point(0, 0);
    this.windowHandle = 0;
  }

  private @NotNull GLCanvas createCanvas() {
    final GLProfile glprofile = GLProfile.getMaxFixedFunc(true);
    final GLCapabilities glcapabilities = new GLCapabilities(glprofile);
    return new RenderCanvas(glcapabilities);
  }

  final class RenderCanvas extends GLCanvas {

    public RenderCanvas(@NotNull final GLCapabilities glcapabilities) {
      super(glcapabilities);
      this.addGLEventListener(
          new GLEventListener() {
            @Override
            public void reshape(
                final GLAutoDrawable glautodrawable,
                final int x,
                final int y,
                final int width,
                final int height) {
              MinecraftBrowser.this.browserRect.setBounds(x, y, width, height);
              MinecraftBrowser.this.screenPoint =
                  MinecraftBrowser.this.canvas.getLocationOnScreen();
              MinecraftBrowser.this.wasResized(width, height);
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
    }

    @Override
    public void paint(final Graphics g) {
      MinecraftBrowser.this.createBrowserIfRequired();
      if (g instanceof Graphics2D) {
        final GraphicsConfiguration config = ((Graphics2D) g).getDeviceConfiguration();
        MinecraftBrowser.this.depth = config.getColorModel().getPixelSize();
        MinecraftBrowser.this.componentDepth = config.getColorModel().getComponentSize()[0];
        MinecraftBrowser.this.scaleFactor = ((Graphics2D) g).getTransform().getScaleX();
      }
      super.paint(g);
    }
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

  private void createBrowserIfRequired() {
    final long windowHandle = this.getWindowHandle();
    if (this.getNativeRef("CefBrowser") == 0) {
      final CefBrowser_N parent = this.getParentBrowser();
      final CefClient client = this.getClient();
      final CefRequestContext context = this.getRequestContext();
      final Point inspect = this.getInspectAt();
      final String url = this.getUrl();
      if (this.getParentBrowser() != null) {
        this.createDevTools(parent, client, windowHandle, true, true, this.canvas, inspect);
      } else {
        this.createBrowser(client, windowHandle, url, true, true, this.canvas, context);
      }
    }
  }

  private long getWindowHandle() {
    if (this.windowHandle == 0) {
      final NativeSurface surface = this.canvas.getNativeSurface();
      if (surface != null) {
        surface.lockSurface();
        this.windowHandle = this.getWindowHandle(surface.getSurfaceHandle());
        surface.unlockSurface();
        assert (this.windowHandle != 0);
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
    this.createBrowserIfRequired();
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
    return new MinecraftBrowser(this.neon, this.settings, this.method, url);
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
    if (this.method != null) {
      this.method.render(this.swapBufferFromBgraToRgba(buffer, width, height));
    }
  }

  private IntBuffer swapBufferFromBgraToRgba(
      @NotNull final ByteBuffer src, final int width, final int height) {
    final int length = width * height;
    final IntBuffer dest = IntBuffer.allocate(length);
    for (int i = 0; i < length; i++) {
      final int bgra = src.getInt(i);
      final int rgba = (bgra & 0x00ff0000) >> 16 | (bgra & 0xff00ff00) | (bgra & 0x000000ff) << 16;
      dest.put(rgba);
    }
    return dest;
  }

  public ByteBuf retrieveFrame(@NotNull final GL2 gl2, final int height, final int width) {
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
    return Unpooled.wrappedBuffer(buffer);
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
      final CefBrowser_N parent = (CefBrowser_N) browser;
      parent.invalidate();
    }
  }

  @Override
  public void onPopupSize(@NotNull final CefBrowser browser, @NotNull final Rectangle size) {
    this.nativeRenderer.onPopupSize(size);
  }

  @Override
  public boolean onCursorChange(@NotNull final CefBrowser browser, final int cursorType) {
    if (this.canvas != null) {
      this.canvas.setCursor(new Cursor(cursorType));
    }
    return true;
  }

  @Override
  public boolean startDragging(
      @NotNull final CefBrowser browser,
      @NotNull final CefDragData dragData,
      final int mask,
      final int x,
      final int y) {
    return true;
  }

  @Override
  public void updateDragCursor(@NotNull final CefBrowser browser, final int operation) {}

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
