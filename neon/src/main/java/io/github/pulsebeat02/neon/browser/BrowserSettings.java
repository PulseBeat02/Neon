/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BrowserSettings {

  private @NotNull final UUID[] players;
  private @NotNull final String character;
  private @NotNull final ImmutableDimension resolution;
  private @NotNull final ImmutableDimension blockDimension;
  private @NotNull final DitherHandler handler;
  private @Nullable final Location location;

  public BrowserSettings(
      @NotNull final ImmutableDimension resolution,
      @NotNull final ImmutableDimension blockDimension,
      @NotNull final String character,
      @Nullable final Location location,
      @NotNull final DitherHandler handler) {
    this.players = this.getAllPlayerUUIDs();
    this.resolution = resolution;
    this.blockDimension = blockDimension;
    this.character = character;
    this.location = location;
    this.handler = handler;
  }

  public static @NotNull BrowserSettings ofSettings(
      @NotNull final BrowserConfiguration configuration) {
    final ImmutableDimension blockDimension = configuration.getBlockDimension();
    final ImmutableDimension dimension = configuration.getResolution();
    final DitherHandler handler = configuration.getAlgorithm().getHandler();
    final String character = configuration.getCharacter();
    final Location location = configuration.getLocation();
    return new BrowserSettings(dimension, blockDimension, character, location, handler);
  }

  private @NotNull UUID[] getAllPlayerUUIDs() {
    return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toArray(UUID[]::new);
  }

  public @NotNull UUID[] getPlayers() {
    return this.players;
  }

  public @NotNull ImmutableDimension getResolution() {
    return this.resolution;
  }

  public @NotNull DitherHandler getHandler() {
    return this.handler;
  }

  public @NotNull String getCharacter() {
    return this.character;
  }

  public @Nullable Location getLocation() {
    return this.location;
  }

  public @NotNull ImmutableDimension getBlockDimension() {
    return this.blockDimension;
  }
}
