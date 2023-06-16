package io.github.pulsebeat02.neon;

import static net.kyori.adventure.text.Component.text;

import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import java.io.IOException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public final class Neon extends JavaPlugin {

  private BrowserConfiguration configuration;
  private BukkitAudiences adventure;
  private Audience console;

  @Override
  public void onEnable() {
    this.adventure = BukkitAudiences.create(this);
    this.console = this.adventure.console();
    this.readConfigurationFile();
  }

  @Override
  public void onDisable() {
    if (this.adventure != null) {
      this.adventure.close();
      this.adventure = null;
    }
  }

  public void readConfigurationFile() {
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

  public @NonNull BukkitAudiences adventure() {
    if (this.adventure == null) {
      throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
    }
    return this.adventure;
  }
}
