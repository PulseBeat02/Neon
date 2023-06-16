package io.github.pulsebeat02.neon.config;

import com.moandjiezana.toml.Toml;
import org.jetbrains.annotations.NotNull;

public final class TomlProvider {

  private @NotNull static final Toml TOML;

  static {
    TOML = new Toml();
  }

  public @NotNull static Toml getToml() {
    return TOML;
  }
}
