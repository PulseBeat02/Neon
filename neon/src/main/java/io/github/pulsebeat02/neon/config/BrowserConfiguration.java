package io.github.pulsebeat02.neon.config;

import com.moandjiezana.toml.Toml;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class BrowserConfiguration {
  private final Path configurationPath;
  private String homePageUrl;

  public BrowserConfiguration(@NotNull final Neon neon) throws IOException {
    this.configurationPath = neon.getDataFolder().toPath().resolve("neon.toml");
    this.checkFile();
    this.readFile();
  }

  private void readFile() {
    final File file = this.configurationPath.toFile();
    final Toml toml = TomlProvider.getToml().read(file);
    this.homePageUrl = toml.getString("homepage_url");
  }

  private void checkFile() throws IOException {
    if (Files.notExists(this.configurationPath)) {
      this.copyFile();
    }
  }

  private void copyFile() throws IOException {
    try (final InputStream stream = ResourceUtils.getResourceAsStream("neon.toml")) {
      Files.copy(stream, this.configurationPath);
    }
  }

  public @NotNull String getHomePageUrl() {
    return this.homePageUrl;
  }
}
