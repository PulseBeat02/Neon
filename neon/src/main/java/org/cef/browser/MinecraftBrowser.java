package org.cef.browser;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.CefProgressHandler;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
  private @NotNull final BrowserSettings settings;
  private @NotNull final RenderMethod method;
  private @NotNull final Rectangle viewArea;
  private @Nullable CefRequestContext context;
  private @Nullable CefBrowser_N parent;
  private final @NotNull Point screenPoint;
  private final @NotNull Rectangle browserRect;
  private final int depth;
  private final int componentDepth;
  private final double scaleFactor;

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
    this.viewArea = this.getViewArea();
    this.depth = 32;
    this.componentDepth = 8;
    this.scaleFactor = 1.0;
    this.browserRect = new Rectangle(0, 0, 1, 1);
    this.screenPoint = new Point(0, 0);
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
    final CefApp app = builder.build();
    app.setSettings(settings);
    return app;
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
        this.createDevTools(parent, client, windowHandle, true, true, null, inspect);
      } else {
        this.createBrowser(client, windowHandle, url, true, true, null, context);
      }
    }
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
    final int length = width * height;
    final int[] bufferArray = new int[length];
    for (int i = 0; i < length; i++) {
      final int bgra = buffer.getInt();
      final int blue = bgra >> 24 & 0xFF;
      final int green = bgra >> 16 & 0xFF;
      final int red = bgra >> 8 & 0xFF;
      final int alpha = bgra & 0xFF;
      bufferArray[i] =
          (red * alpha / 255) << 16 | (green * alpha / 255) << 8 | (blue * alpha / 255);
    }
    this.method.render(IntBuffer.wrap(bufferArray));
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
    if (!show && this.parent != null) {
      this.parent.invalidate();
    }
  }

  @Override
  public void onPopupSize(@NotNull final CefBrowser browser, @NotNull final Rectangle size) {}

  @Override
  public boolean onCursorChange(@NotNull final CefBrowser browser, final int cursorType) {
    return true;
  }

  @Override
  public void updateDragCursor(final CefBrowser browser, final int operation) {}

  @Override
  public boolean startDragging(
      final CefBrowser browser,
      final CefDragData dragData,
      final int mask,
      final int x,
      final int y) {
    return true;
  }

  public void sendNativeMouseEvent(final int x, final int y, final int type) {
    final JPanel fake = new JPanel();
    final Rectangle rectangle = this.browserRect;
    final double width = rectangle.getWidth();
    final double height = rectangle.getHeight();
    final Dimension size = new Dimension((int) width, (int) height);
    fake.setSize(size);
    final MouseEvent event =
        new MouseEvent(fake, 0, System.currentTimeMillis(), 0, x, y, 1, false, type);
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
