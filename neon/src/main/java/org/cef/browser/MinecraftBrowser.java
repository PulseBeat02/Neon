package org.cef.browser;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.CefProgressHandler;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
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

public class MinecraftBrowser extends CefBrowser_N implements CefRenderHandler {

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
  private @NotNull final BrowserSettings settings;

  private @NotNull final RenderMethod method;
  private CefRenderHandler renderer;

  public MinecraftBrowser(
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url) {
    super(CEF_CLIENT, url, null, null, null);
    this.neon = neon;
    this.settings = settings;
    this.method = method;
    this.renderer = new MinecraftBrowserRenderer(settings, method);
    this.setupBrowser(settings);
  }

  private MinecraftBrowser(
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final CefClient client,
      @NotNull final String url,
      @NotNull final CefRequestContext context,
      @NotNull final CefRenderHandler renderer,
      @NotNull final CefBrowser_N parent,
      @NotNull final Point inspectAt) {
    super(client, url, context, parent, inspectAt);
    this.neon = neon;
    this.settings = settings;
    this.method = method;
    this.renderer = renderer;
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
    builder.addJcefArgs("--show-fps-counter");
    builder.addJcefArgs("--enable-begin-frame-scheduling");
    builder.addJcefArgs("--off-screen-frame-rate=20");
    return builder.build();
  }

  @Override
  public void close(final boolean force) {
    super.close(force);
    this.renderer = null;
  }

  private void setupBrowser(@NotNull final BrowserSettings settings) {
    final ImmutableDimension dimension = settings.getResolution();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    this.setFocus(true);
    this.wasResized(width, height);
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
    return new MinecraftBrowser(
        this.neon,
        this.settings,
        this.method,
        client,
        url,
        context,
        this.renderer,
        parent,
        inspectAt);
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
    return this.renderer;
  }

  @Override
  public Rectangle getViewRect(final CefBrowser browser) {
    return this.renderer.getViewRect(browser);
  }

  @Override
  public boolean getScreenInfo(final CefBrowser browser, final CefScreenInfo screenInfo) {
    return this.renderer.getScreenInfo(browser, screenInfo);
  }

  @Override
  public Point getScreenPoint(final CefBrowser browser, final Point viewPoint) {
    return this.renderer.getScreenPoint(browser, viewPoint);
  }

  @Override
  public void onPopupShow(final CefBrowser browser, final boolean show) {
    this.renderer.onPopupShow(browser, show);
  }

  @Override
  public void onPopupSize(final CefBrowser browser, final Rectangle size) {
    this.renderer.onPopupSize(browser, size);
  }

  @Override
  public void onPaint(
      final CefBrowser browser,
      final boolean popup,
      final Rectangle[] dirtyRects,
      final ByteBuffer buffer,
      final int width,
      final int height) {
    this.renderer.onPaint(browser, popup, dirtyRects, buffer, width, height);
  }

  @Override
  public boolean onCursorChange(final CefBrowser browser, final int cursorType) {
    return this.renderer.onCursorChange(browser, cursorType);
  }

  @Override
  public boolean startDragging(
      final CefBrowser browser,
      final CefDragData dragData,
      final int mask,
      final int x,
      final int y) {
    return this.renderer.startDragging(browser, dragData, mask, x, y);
  }

  @Override
  public void updateDragCursor(final CefBrowser browser, final int operation) {
    this.renderer.updateDragCursor(browser, operation);
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
