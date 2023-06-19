package org.cef.browser;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.netty.buffer.ByteBuf;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.jetbrains.annotations.NotNull;

public final class MinecraftBrowserRenderer implements CefRenderHandler {

  private static @NotNull final Point INITIAL_POINT;
  private static @NotNull final Rectangle BROWSER_RECTANGLE;

  static {
    INITIAL_POINT = new Point(0, 0);
    BROWSER_RECTANGLE = new Rectangle(0, 0, 1, 1);
  }

  private @NotNull final Rectangle viewArea;
  private @NotNull final BrowserSettings settings;
  private @NotNull final PacketSender sender;

  public MinecraftBrowserRenderer(
      @NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    final ImmutableDimension dimension = settings.getDimension();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    this.viewArea = new Rectangle(0, 0, width, height);
    this.settings = settings;
    this.sender = neon.getPacketSender();
  }

  @Override
  public void onPaint(
      @NotNull final CefBrowser browser,
      final boolean popup,
      @NotNull final Rectangle[] dirtyRects,
      @NotNull final ByteBuffer buffer,
      final int width,
      final int height) {
    final DitherHandler handler = this.settings.getHandler();
    final int blockWidth = this.settings.getBlockWidth();
    final int blockHeight = this.settings.getBlockHeight();
    final ByteBuf buf = handler.dither(buffer, blockWidth);
    this.sender.displayMaps(null, buf, 0, blockWidth, blockHeight, width, height);
  }

  @Override
  public @NotNull Rectangle getViewRect(@NotNull final CefBrowser browser) {
    return this.viewArea;
  }

  @Override
  public boolean getScreenInfo(
      @NotNull final CefBrowser browser, @NotNull final CefScreenInfo screenInfo) {
    final Rectangle bounds = BROWSER_RECTANGLE.getBounds();
    screenInfo.Set(1, 32, 8, false, bounds, bounds);
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
  public void onPopupSize(@NotNull final CefBrowser browser, @NotNull final Rectangle size) {}

  @Override
  public boolean onCursorChange(@NotNull final CefBrowser browser, final int cursorType) {
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
}
