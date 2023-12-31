/*
 * MIT License
 *
 * Copyright (c) 2024 Brandon Li
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
package io.github.pulsebeat02.neon.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.browser.configure.EntitySelection;
import io.github.pulsebeat02.neon.dither.Algorithm;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class BrowserConfiguration {

  private static @NotNull final ScheduledExecutorService SCHEDULER_EXECUTOR;

  static {
    SCHEDULER_EXECUTOR = Executors.newScheduledThreadPool(1);
  }

  private @NotNull final Path configurationPath;
  private @NotNull Algorithm algorithm;
  private @NotNull ImmutableDimension resolution;
  private @NotNull ImmutableDimension blockDimension;
  private @NotNull String character;
  private @Nullable Location location;
  private @NotNull EntitySelection selection;

  public BrowserConfiguration(@NotNull final Neon neon) {
    this.configurationPath = neon.getDataFolder().toPath().resolve("neon.toml");
    this.checkFile();
    this.algorithm = Algorithm.FILTER_LITE;
    this.resolution = new ImmutableDimension(640, 640);
    this.blockDimension = new ImmutableDimension(5, 5);
    this.character = "█";
    this.selection = EntitySelection.HOLOGRAM;
    this.readFile();
    this.savePeriodically();
  }

  public void shutdownConfiguration() {
    this.saveFile();
    SCHEDULER_EXECUTOR.shutdown();
  }

  private void savePeriodically() {
    final Runnable task = this::saveFile;
    SCHEDULER_EXECUTOR.scheduleAtFixedRate(task, 10, 10, TimeUnit.MINUTES);
  }

  private void saveFile() {
    try {
      this.checkFile();
      this.printFile();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void printFile() throws IOException {

    // i know this is messed up but...
    class NeonTable {
      final Map<String, Object> neon = new HashMap<>();
    }

    final NeonTable configTable = new NeonTable();
    final Map<String, Object> table = configTable.neon;
    table.put("algorithm", this.algorithm.name());
    table.put("resolution", this.createDimension(this.resolution));
    table.put("block_dimension", this.createDimension(this.blockDimension));
    table.put("character", this.character);
    table.put("selection", this.selection.name());

    final File file = this.configurationPath.toFile();
    final TomlWriter writer = TomlProvider.getTomlWriter();
    writer.write(configTable, file);
  }

  private @NotNull @Unmodifiable List<Integer> createDimension(
      @NotNull final ImmutableDimension dimension) {
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    return List.of(width, height);
  }

  private void readFile() {
    final File file = this.configurationPath.toFile();
    final Toml toml = TomlProvider.getToml().read(file);
    this.algorithm = this.parseAlgorithm(toml);
    this.resolution = this.parseDimension(toml, "neon.resolution");
    this.blockDimension = this.parseDimension(toml, "neon.block_dimension");
    this.character = toml.getString("neon.character");
    this.selection = this.parseEntitySelection(toml);
  }

  private @NotNull EntitySelection parseEntitySelection(@NotNull final Toml toml) {
    return EntitySelection.ofKey(toml.getString("neon.entity_selection"))
        .orElse(EntitySelection.HOLOGRAM);
  }

  private @NotNull Algorithm parseAlgorithm(@NotNull final Toml toml) {
    return Algorithm.ofKey(toml.getString("neon.algorithm")).orElse(Algorithm.FILTER_LITE);
  }

  private @NotNull ImmutableDimension parseDimension(
      @NotNull final Toml toml, @NotNull final String key) {
    final List<Long> list = toml.getList(key);
    final int width = list.get(0).intValue();
    final int height = list.get(1).intValue();
    return new ImmutableDimension(width, height);
  }

  private void checkFile() {
    if (Files.notExists(this.configurationPath)) {
      try {
        this.copyFile();
      } catch (final IOException e) {
        throw new AssertionError(e);
      }
    }
  }

  private void copyFile() throws IOException {
    this.createFolders();
    try (final InputStream stream = ResourceUtils.getResourceAsStream("config/neon.toml")) {
      Files.copy(stream, this.configurationPath);
    }
  }

  private void createFolders() throws IOException {
    final Path parent = this.configurationPath.getParent();
    if (Files.notExists(parent)) {
      Files.createDirectories(parent);
    }
  }

  public @NotNull Path getConfigurationPath() {
    return this.configurationPath;
  }

  public @NotNull Algorithm getAlgorithm() {
    return this.algorithm;
  }

  public void setAlgorithm(@NotNull final Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  public @NotNull ImmutableDimension getResolution() {
    return this.resolution;
  }

  public void setResolution(@NotNull final ImmutableDimension resolution) {
    this.resolution = resolution;
  }

  public @NotNull ImmutableDimension getBlockDimension() {
    return this.blockDimension;
  }

  public void setBlockDimension(@NotNull final ImmutableDimension blockDimension) {
    this.blockDimension = blockDimension;
  }

  public @NotNull String getCharacter() {
    return this.character;
  }

  public void setCharacter(@NotNull final String character) {
    this.character = character;
  }

  public @NotNull EntitySelection getSelection() {
    return this.selection;
  }

  public void setEntitySelection(@NotNull final EntitySelection selection) {
    this.selection = selection;
  }

  public @Nullable Location getLocation() {
    return this.location;
  }

  public void setLocation(@NotNull final Location location) {
    this.location = location;
  }
}
