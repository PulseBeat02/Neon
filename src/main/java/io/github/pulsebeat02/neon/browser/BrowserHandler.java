package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import java.io.IOException;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.jetbrains.annotations.NotNull;

public final class MinecraftBrowser {

  private final Neon neon;
  private final CefApp app;
  private final CefClient client;
  private final CefBrowser browser;

  public MinecraftBrowser(@NotNull final Neon neon, @NotNull final CefApp app)
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    this.neon = neon;
    this.app = this.createApp();
    this.client = app.createClient();
    this.browser = this.createBrowser();
  }

  private CefBrowser createBrowser() {
    final BrowserConfiguration configuration = this.neon.getConfiguration();
    final String url = configuration.getHomePageUrl();
    return this.client.createBrowser(url, true, true);
  }

  private CefApp createApp()
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    final CefAppBuilder builder = new CefAppBuilder();
    builder.setProgressHandler(new CefProgressHandler(this.neon));
    builder.addJcefArgs("--disable-gpu");
    builder.addJcefArgs("--disable-gpu-compositing");
    return builder.build();
  }
}
