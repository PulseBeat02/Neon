package io.github.pulsebeat02.neon.config;

import com.moandjiezana.toml.Toml;

public final class TomlProvider {

  private static final Toml TOML;

  static {
    TOML = new Toml();
  }

  public static Toml getToml() {
    return TOML;
  }
}
