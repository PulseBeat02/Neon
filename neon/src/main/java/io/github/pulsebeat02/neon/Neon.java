package io.github.pulsebeat02.neon;

import static net.kyori.adventure.text.Component.text;

import io.github.pulsebeat02.neon.browser.SeleniumBrowser;
import io.github.pulsebeat02.neon.command.CommandHandler;
import io.github.pulsebeat02.neon.command.browser.ExecutorProvider;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.dither.MapPalette;
import io.github.pulsebeat02.neon.event.BrowserClickListener;
import io.github.pulsebeat02.neon.event.PlayerHookListener;
import io.github.pulsebeat02.neon.locale.AudienceHandler;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.nms.ReflectionHandler;
import io.github.pulsebeat02.neon.utils.ProcessUtils;
import java.io.IOException;
import java.nio.file.Path;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Neon extends JavaPlugin {

  private static final int BSTATS_PLUGIN_ID;
  private static final String SELENIUM_CACHE;

  static {
    BSTATS_PLUGIN_ID = 18773;
    SELENIUM_CACHE = "SE_CACHE_PATH";
  }

  private BrowserConfiguration configuration;
  private PacketSender sender;
  private AudienceHandler audience;
  private SeleniumBrowser browser;

  @Override
  public void onLoad() {
    this.registerStaticBlocks();
    this.setSeleniumFolder();
  }

  @Override
  public void onEnable() {
    this.registerAdventure();
    this.registerServerImplementation();
    this.readConfigurationFile();
    this.registerCommands();
    this.registerStats();
    this.registerEvents();
  }

  @Override
  public void onDisable() {
    this.shutdownBrowser();
    this.shutdownConfiguration();
    this.shutdownAdventure();
    this.shutdownExecutors();
  }

  private void setSeleniumFolder() {
    final Path parent = this.getDataFolder().toPath();
    final Path selenium = parent.resolve("selenium");
    final String path = selenium.toAbsolutePath().toString();
    ProcessUtils.setEnvironmentalVariable(SELENIUM_CACHE, path);
  }

  private void registerStaticBlocks() {
    ExecutorProvider.init();
    MapPalette.init();
  }

  private void registerAdventure() {
    this.audience = new AudienceHandler(this);
  }

  private void registerServerImplementation() {
    this.sender = new ReflectionHandler(this).getNewPacketHandlerInstance();
  }

  private void shutdownAdventure() {
    this.audience.shutdown();
  }

  private void shutdownExecutors() {
    ExecutorProvider.shutdown();
  }

  private void shutdownConfiguration() {
    this.configuration.shutdownConfiguration();
  }

  private void registerEvents() {
    new PlayerHookListener(this);
    new BrowserClickListener(this);
  }

  private void registerStats() {
    new Metrics(this, BSTATS_PLUGIN_ID);
  }

  private void registerCommands() {
    new CommandHandler(this);
  }

  private void readConfigurationFile() {
    try {
      this.configuration = new BrowserConfiguration(this);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void logConsole(@NotNull final String text) {
    final Audience console = this.audience.console();
    console.sendMessage(text(text));
  }

  public @NotNull BrowserConfiguration getConfiguration() {
    return this.configuration;
  }

  public @NotNull BukkitAudiences audience() {
    return this.audience.retrieve();
  }

  public @NotNull PacketSender getPacketSender() {
    return this.sender;
  }

  public void shutdownBrowser() {
    if (this.browser != null) {
      this.browser.shutdown();
      this.browser = null;
    }
  }

  public SeleniumBrowser getBrowser() {
    return this.browser;
  }

  public void setBrowser(@NotNull final SeleniumBrowser browser) {
    this.shutdownBrowser();
    this.browser = browser;
  }
}
