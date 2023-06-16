package io.github.pulsebeat02.neon.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import org.jetbrains.annotations.NotNull;

public final class TomlProvider {

  private @NotNull static final Toml TOML;
  private @NotNull static final TomlWriter TOML_WRITER;

  static {
    TOML = new Toml();
    TOML_WRITER = new TomlWriter.Builder().indentValuesBy(2).build();
  }

  public @NotNull static Toml getToml() {
    return TOML;
  }

  public @NotNull static TomlWriter getTomlWriter() {
    return TOML_WRITER;
  }
}
