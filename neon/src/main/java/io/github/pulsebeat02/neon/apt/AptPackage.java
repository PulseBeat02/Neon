package io.github.pulsebeat02.neon.apt;

import org.jetbrains.annotations.NotNull;

public final class AptPackage {

  private @NotNull final String name;
  private @NotNull final String append;

  public AptPackage(@NotNull final String name, @NotNull final String append) {
    this.name = name;
    this.append = append;
  }

  public @NotNull String getName() {
    return this.name;
  }

  public @NotNull String getAppend() {
    return this.append;
  }
}
