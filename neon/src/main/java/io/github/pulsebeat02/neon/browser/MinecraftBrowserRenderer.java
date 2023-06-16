package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.ImmutableDimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.jetbrains.annotations.NotNull;

public class MinecraftBrowserRenderer implements CefRenderHandler {

  private static final Point INITIAL_POINT;

  static {
    INITIAL_POINT = new Point(0,0);
  }

  private final Rectangle viewArea;
  private final DitherHandler ditherHandler;

  public MinecraftBrowserRenderer(@NotNull final ImmutableDimension dimension, @NotNull final DitherHandler ditherHandler) {
    final int x = dimension.getX();
    final int y = dimension.getY();
    this.viewArea = new Rectangle(0,0,x,y);
    this.ditherHandler = ditherHandler;
  }

  @Override
  public void onPaint(final CefBrowser browser, final boolean popup, final Rectangle[] dirtyRects, final ByteBuffer buffer,
      final int width, final int height) {

  }

  @Override
  public Rectangle getViewRect(final CefBrowser browser) {
    return this.viewArea;
  }

  @Override
  public boolean getScreenInfo(final CefBrowser browser, final CefScreenInfo screenInfo) {
    return browser.getScreenInfo(cefBrowser, cefScreenInfo);
  }

  @Override
  public Point getScreenPoint(final CefBrowser browser, final Point viewPoint) {
    final Point point = new Point(INITIAL_POINT);
    point.translate(viewPoint.x, viewPoint.y);
    return point;
  }

  @Override
  public void onPopupShow(final CefBrowser browser, final boolean show) {
  }

  @Override
  public void onPopupSize(final CefBrowser browser, final Rectangle size) {
  }

  @Override
  public boolean onCursorChange(final CefBrowser browser, final int cursorType) {
    return false;
  }

  @Override
  public boolean startDragging(final CefBrowser browser, final CefDragData dragData, final int mask, final int x, final int y) {
    return false;
  }

  @Override
  public void updateDragCursor(final CefBrowser browser, final int operation) {
  }
}
