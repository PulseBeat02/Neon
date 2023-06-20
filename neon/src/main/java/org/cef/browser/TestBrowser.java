package org.cef.browser;


import io.github.pulsebeat02.neon.dither.algorithm.error.FilterLiteDither;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.jetbrains.annotations.NotNull;

public class TestBrowser extends CefBrowser_N implements CefBrowser, CefRenderHandler {

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

  private final FilterLiteDither dither;
  private final Consumer<ByteBuf> buffer;

  public TestBrowser(@NotNull final String url, @NotNull final Consumer<ByteBuf> buffer) {
    super(CEF_CLIENT, url, null, null, null);
    this.dither = new FilterLiteDither();
    this.buffer = buffer;
  }

  private TestBrowser(
      @NotNull final CefClient client,
      @NotNull final String url,
      @NotNull final CefRequestContext context,
      @NotNull final CefBrowser_N parent,
      @NotNull final Point inspectAt,
      @NotNull final Consumer<ByteBuf> buffer) {
    super(client, url, context, parent, inspectAt);
    this.dither = new FilterLiteDither();
    this.buffer = buffer;
  }

  private static @NotNull CefApp createApp()
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    final CefAppBuilder builder = new CefAppBuilder();
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

  @Override
  public void createImmediately() {
    this.createBrowser();
  }

  @Override
  public Component getUIComponent() {
    return null;
  }

  @Override
  public CefBrowser_N createDevToolsBrowser(
      final CefClient client,
      final String url,
      final CefRequestContext context,
      final CefBrowser_N parent,
      final Point inspectAt) {
    return new TestBrowser(client, url, context, parent, inspectAt, this.buffer);
  }

  private void createBrowser() {
    if (this.getNativeRef("CefBrowser") == 0) {
      if (this.getParentBrowser() != null) {
        this.createDevTools(
            this.getParentBrowser(), this.getClient(), 0, true, true, null, this.getInspectAt());
      } else {
        this.createBrowser(
            this.getClient(), 0, this.getUrl(), true, true, null, this.getRequestContext());
      }
    }
  }

  @Override
  public CompletableFuture<BufferedImage> createScreenshot(final boolean nativeResolution) {
    throw new UnsupportedOperationException("Screenshot not supported!");
  }

  @Override
  public CefRenderHandler getRenderHandler() {
    return null;
  }

  @Override
  public Rectangle getViewRect(final CefBrowser browser) {
    return new Rectangle(640, 640);
  }

  @Override
  public boolean getScreenInfo(final CefBrowser browser, final CefScreenInfo screenInfo) {
    return false;
  }

  @Override
  public Point getScreenPoint(final CefBrowser browser, final Point viewPoint) {
    return new Point(0, 0);
  }

  @Override
  public void onPopupShow(final CefBrowser browser, final boolean show) {}

  @Override
  public void onPopupSize(final CefBrowser browser, final Rectangle size) {}

  @Override
  public void onPaint(
      final CefBrowser browser,
      final boolean popup,
      final Rectangle[] dirtyRects,
      final ByteBuffer buffer,
      final int width,
      final int height) {
    this.buffer.accept(this.dither.dither(Unpooled.wrappedBuffer(buffer), width));
  }

  @Override
  public boolean onCursorChange(final CefBrowser browser, final int cursorType) {
    return false;
  }

  @Override
  public boolean startDragging(
      final CefBrowser browser,
      final CefDragData dragData,
      final int mask,
      final int x,
      final int y) {
    return false;
  }

  @Override
  public void updateDragCursor(final CefBrowser browser, final int operation) {}
}
