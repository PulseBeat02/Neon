package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.UUID;

import io.github.pulsebeat02.neon.utils.immutable.ImmutableLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BrowserSettings {

  private @NotNull final UUID[] players;
  private final int blockWidth;
  private final int blockHeight;
  private @NotNull final String character;
  private @NotNull final ImmutableDimension dimension;
  private @NotNull final DitherHandler handler;
  private @NotNull final Location location;

  public BrowserSettings(
      final int blockWidth,
      final int blockHeight,
      @NotNull final ImmutableDimension dimension,
      @NotNull final String character,
      @NotNull final Location location,
      @NotNull final DitherHandler handler) {
    this.players = this.getAllPlayerUUIDs();
    this.blockWidth = blockWidth;
    this.blockHeight = blockHeight;
    this.dimension = dimension;
    this.character = character;
    this.location = location;
    this.handler = handler;
  }

  public static @NotNull BrowserSettings ofSettings(
      @NotNull final BrowserConfiguration configuration) {
    final int blockWidth = configuration.getBlockWidth();
    final int blockHeight = configuration.getBlockHeight();
    final ImmutableDimension dimension = configuration.getDimension();
    final DitherHandler handler = configuration.getAlgorithm().getHandler();
    final String character = configuration.getCharacter();
    final Location location = configuration.getLocation();
    return new BrowserSettings(blockWidth, blockHeight, dimension, character, location, handler);
  }

  private @NotNull UUID[] getAllPlayerUUIDs() {
    return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toArray(UUID[]::new);
  }

  public @NotNull UUID[] getPlayers() {
    return this.players;
  }

  public int getBlockWidth() {
    return this.blockWidth;
  }

  public @NotNull ImmutableDimension getDimension() {
    return this.dimension;
  }

  public @NotNull DitherHandler getHandler() {
    return this.handler;
  }

  public int getBlockHeight() {
    return this.blockHeight;
  }

  public @NotNull String getCharacter() {
    return this.character;
  }

  public @NotNull Location getLocation() {
    return this.location;
  }
}
