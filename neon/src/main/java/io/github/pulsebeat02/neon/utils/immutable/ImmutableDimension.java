package io.github.pulsebeat02.neon.utils;

public final class ImmutableDimension {

  private final int x;
  private final int y;

  public ImmutableDimension(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }
}
