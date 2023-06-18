package io.github.pulsebeat02.neon.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.pulsebeat02.neon.Neon;
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
import org.jetbrains.annotations.NotNull;

public final class BrowserConfiguration {

  private static @NotNull final ScheduledExecutorService SCHEDULER_EXECUTOR;

  static {
    SCHEDULER_EXECUTOR = Executors.newScheduledThreadPool(1);
  }

  private @NotNull final Path configurationPath;
  private @NotNull String homePageUrl;
  private @NotNull Algorithm algorithm;
  private @NotNull ImmutableDimension dimension;
  private int blockWidth;

  private int blockHeight;

  public BrowserConfiguration(@NotNull final Neon neon) throws IOException {
    this.configurationPath = neon.getDataFolder().toPath().resolve("neon.toml");
    this.checkFile();
    this.homePageUrl = "https://www.google.com";
    this.algorithm = Algorithm.FILTER_LITE;
    this.dimension = new ImmutableDimension(640, 640);
    this.blockWidth = 5;
    this.readFile();
    this.savePeriodically();
  }

  public void shutdownConfiguration() {
    this.saveFile();
    SCHEDULER_EXECUTOR.shutdown();
  }

  private void savePeriodically() {
    SCHEDULER_EXECUTOR.scheduleAtFixedRate(this::saveFile, 10, 10, TimeUnit.MINUTES);
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
    table.put("homepage_url", this.homePageUrl);
    table.put("algorithm", this.algorithm.name());
    table.put("dimension", List.of(this.dimension.getWidth(), this.dimension.getHeight()));
    table.put("block_width", this.blockWidth);
    table.put("block_height", this.blockHeight);

    final File file = this.configurationPath.toFile();
    final TomlWriter writer = TomlProvider.getTomlWriter();
    writer.write(configTable, file);
  }

  private void readFile() {
    final File file = this.configurationPath.toFile();
    final Toml toml = TomlProvider.getToml().read(file);
    this.homePageUrl = toml.getString("neon.homepage_url");
    this.algorithm = this.parseAlgorithm(toml);
    this.dimension = this.parseDimension(toml);
    this.blockWidth = toml.getLong("neon.block_width").intValue();
    this.blockHeight = toml.getLong("neon.block_height").intValue();
  }

  private @NotNull Algorithm parseAlgorithm(@NotNull final Toml toml) {
    return Algorithm.ofKey(toml.getString("neon.algorithm")).orElse(Algorithm.FILTER_LITE);
  }

  private @NotNull ImmutableDimension parseDimension(@NotNull final Toml toml) {
    final List<Long> list = toml.getList("neon.dimension");
    final int width = list.get(0).intValue();
    final int height = list.get(1).intValue();
    return new ImmutableDimension(width, height);
  }

  private void checkFile() throws IOException {
    if (Files.notExists(this.configurationPath)) {
      this.copyFile();
    }
  }

  private void copyFile() throws IOException {
    this.createFolders();
    try (final InputStream stream = ResourceUtils.getResourceAsStream("neon.toml")) {
      Files.copy(stream, this.configurationPath);
    }
  }

  private void createFolders() throws IOException {
    final Path parent = this.configurationPath.getParent();
    if (Files.notExists(parent) || !Files.isDirectory(parent)) {
      Files.createDirectories(parent);
    }
  }

  public @NotNull String getHomePageUrl() {
    return this.homePageUrl;
  }

  public @NotNull Path getConfigurationPath() {
    return this.configurationPath;
  }

  public @NotNull Algorithm getAlgorithm() {
    return this.algorithm;
  }

  public @NotNull ImmutableDimension getDimension() {
    return this.dimension;
  }

  public int getBlockWidth() {
    return this.blockWidth;
  }

  public void setHomePageUrl(@NotNull final String homePageUrl) {
    this.homePageUrl = homePageUrl;
  }

  public void setAlgorithm(@NotNull final Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  public void setDimension(@NotNull final ImmutableDimension dimension) {
    this.dimension = dimension;
  }

  public void setBlockWidth(final int blockWidth) {
    this.blockWidth = blockWidth;
  }

  public int getBlockHeight() {
    return this.blockHeight;
  }

  public void setBlockHeight(final int blockHeight) {
    this.blockHeight = blockHeight;
  }
}
