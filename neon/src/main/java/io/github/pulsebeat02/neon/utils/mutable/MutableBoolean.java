package io.github.pulsebeat02.neon.utils.mutable;

import org.jetbrains.annotations.NotNull;

public class MutableBoolean {

  private boolean bool;

  MutableBoolean(final boolean bool) {
    this.bool = bool;
  }

  public static @NotNull MutableBoolean ofBool(final boolean bool) {
    return new MutableBoolean(bool);
  }

  public static @NotNull MutableBoolean ofFalse() {
    return ofBool(false);
  }

  public static @NotNull MutableBoolean ofTrue() {
    return ofBool(true);
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
