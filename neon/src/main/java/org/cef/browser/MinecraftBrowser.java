package org.cef.browser;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.CefProgressHandler;

import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.handler.CefRenderHandler;
import org.jetbrains.annotations.NotNull;

public class MinecraftBrowser extends CefBrowser_N {

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
  private @NotNull final CefRenderHandler renderer;

  public MinecraftBrowser(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(CEF_CLIENT, neon.getConfiguration().getHomePageUrl(), null, null, null);
    this.neon = neon;
    this.renderer = new MinecraftBrowserRenderer(neon, settings);
  }

  private MinecraftBrowser(
      @NotNull final Neon neon,
      @NotNull final CefClient client,
      @NotNull final String url,
      @NotNull final CefRequestContext context,
      @NotNull final CefRenderHandler renderer,
      @NotNull final CefBrowser_N parent,
      @NotNull final Point inspectAt) {
    super(client, url, context, parent, inspectAt);
    this.neon = neon;
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
    builder.addJcefArgs("--disable-gpu-vsync");
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
    return new MinecraftBrowser(this.neon, client, url, context, this.renderer, parent, inspectAt);
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
}
