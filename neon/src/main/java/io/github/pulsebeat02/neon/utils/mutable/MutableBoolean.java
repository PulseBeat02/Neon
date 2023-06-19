package io.github.pulsebeat02.neon.utils.mutable;

import org.jetbrains.annotations.NotNull;

public final class MutableBoolean {

  private boolean bool;

  public MutableBoolean(final boolean bool) {
    this.bool = bool;
  }

  public void flip() {
    this.bool = !this.bool;
  }

  public void set(final boolean newBool) {
    this.bool = newBool;
  }

  public boolean getBoolean() {
    return this.bool;
  }
}
