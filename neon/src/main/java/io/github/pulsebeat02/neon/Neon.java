package io.github.pulsebeat02.neon;

import static net.kyori.adventure.text.Component.text;

import io.github.pulsebeat02.neon.command.CommandHandler;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.event.PlayerHookListener;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.nms.ReflectionHandler;
import java.io.IOException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.cef.browser.MinecraftBrowser;
import org.jetbrains.annotations.NotNull;

public final class Neon extends JavaPlugin {

  private static final int BSTATS_PLUGIN_ID;

  static {
    BSTATS_PLUGIN_ID = 18773;
  }

  private BrowserConfiguration configuration;
  private PacketSender sender;
  private BukkitAudiences audience;
  private Audience console;
  private MinecraftBrowser browser;

  @Override
  public void onEnable() {
    this.registerAdventure();
    this.registerServerImplementation();
    this.readConfigurationFile();
    this.registerCommands();
    this.registerStats();
    this.registerEvents();
  }

  private void registerAdventure() {
    this.audience = BukkitAudiences.create(this);
    this.console = this.audience.console();
  }

  private void registerServerImplementation() {
    this.sender = new ReflectionHandler(this).getNewPacketHandlerInstance();
  }

  @Override
  public void onDisable() {
    this.shutdownBrowser();
    this.shutdownConfiguration();
    this.shutdownAdventure();
  }

  private void shutdownConfiguration() {
    this.configuration.shutdownConfiguration();
  }

  private void shutdownAdventure() {
    if (this.audience != null) {
      this.audience.close();
      this.audience = null;
    }
  }

  private void registerEvents() {
    new PlayerHookListener(this);
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
    this.console.sendMessage(text(text));
  }

  public @NotNull BrowserConfiguration getConfiguration() {
    return this.configuration;
  }

  public @NotNull BukkitAudiences audience() {
    if (this.audience == null) {
      throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
    }
    return this.audience;
  }

  public @NotNull PacketSender getPacketSender() {
    return this.sender;
  }

  private void shutdownBrowser() {
    if (this.browser != null) {
      this.browser.close(true);
      this.browser = null;
    }
  }

  public @NotNull MinecraftBrowser getBrowser() {
    return this.browser;
  }

  public void setBrowser(@NotNull final MinecraftBrowser browser) {
    this.shutdownBrowser();
    this.browser = browser;
  }
}
