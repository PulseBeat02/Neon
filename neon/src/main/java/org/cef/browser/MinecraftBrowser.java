package org.cef.browser;

import static java.util.Objects.requireNonNull;

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
import java.util.concurrent.CompletableFuture;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MinecraftBrowser extends CefBrowser_N implements CefRenderHandler {
  private static @NotNull final CefClient CEF_CLIENT;
  private static @NotNull final Point INITIAL_POINT;
  private static @NotNull final Rectangle BROWSER_RECTANGLE;

  static {
    try {
      final CefApp CEF_APP = createApp();
      CEF_CLIENT = CEF_APP.createClient();
      INITIAL_POINT = new Point(0, 0);
      BROWSER_RECTANGLE = new Rectangle(0, 0, 1, 1);
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
  private int depth;
  private int componentDepth;
  private double scaleFactor;

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
    this.neon = neon;
    this.settings = settings;
    this.method = method;
    this.nativeRenderer = new CefRenderer(true);
    this.viewArea = this.getViewArea();
    this.canvas = this.createCanvas();
    this.handleZoom();
  }

  private @NotNull GLCanvas createCanvas() {
    final GLProfile glprofile = GLProfile.getMaxFixedFunc(true);
    final GLCapabilities glcapabilities = new GLCapabilities(glprofile);
    return new GLCanvas(glcapabilities);
  }

  private void handleZoom() {
    final Graphics g = this.canvas.getGraphics();
    final GraphicsConfiguration config = ((Graphics2D) g).getDeviceConfiguration();
    this.depth = config.getColorModel().getPixelSize();
    this.componentDepth = config.getColorModel().getComponentSize()[0];
    this.scaleFactor = ((Graphics2D) g).getTransform().getScaleX();
  }

  private @NotNull Rectangle getViewArea() {
    final ImmutableDimension dimension = this.settings.getResolution();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    return new Rectangle(0, 0, width, height);
  }

  private static @NotNull CefApp createApp()
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    final CefAppBuilder builder = new CefAppBuilder();
    final Plugin plugin = requireNonNull(Bukkit.getPluginManager().getPlugin("Neon"));
    builder.setProgressHandler(new CefProgressHandler((Neon) plugin));
    builder.addJcefArgs("--disable-gpu-compositing");
    builder.addJcefArgs("--disable-software-rasterizer");
    builder.addJcefArgs("--disable-extensions");
    builder.addJcefArgs("--no-sandbox");
    builder.addJcefArgs("--off-screen-rendering-enabled");
    builder.addJcefArgs("--disable-gpu");
    builder.addJcefArgs("--enable-begin-frame-scheduling");
    builder.addJcefArgs("--off-screen-frame-rate=20");
    return builder.build();
  }

  private void notifyAfterParentChanged() {
    this.getClient().onAfterParentChanged(this);
  }

  private void createBrowserIfRequired() {
    final long windowHandle = 0;
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

  @Override
  public void close(final boolean force) {
    super.close(force);
    this.notifyAfterParentChanged();
    if (this.canvas != null) {
      this.canvas.setEnabled(false);
      this.canvas.destroy();
      this.canvas = null;
    }
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

    final GLContext context = this.canvas != null ? this.canvas.getContext() : null;
    if (context == null) {
      return;
    }

    final GL2 gl2 = this.canvas != null ? this.canvas.getGL().getGL2() : null;
    if (gl2 == null) {
      return;
    }

    this.nativeRenderer.onPaint(
        this.canvas.getGL().getGL2(), popup, dirtyRects, buffer, width, height);
    context.release();

    this.method.render(this.retrieveFrame(gl2, height, width));
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
    final Rectangle bounds = BROWSER_RECTANGLE.getBounds();
    screenInfo.Set(this.scaleFactor, this.depth, this.componentDepth, false, bounds, bounds);
    return true;
  }

  @Override
  public @NotNull Point getScreenPoint(
      @NotNull final CefBrowser browser, @NotNull final Point viewPoint) {
    final Point point = new Point(INITIAL_POINT);
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
