package io.github.pulsebeat02.neon.browser;

public enum MouseClick {
  LEFT(0),
  RIGHT(1),
  DOUBLE(2),
  HOLD(3),
  RELEASE(4);

  private final int id;

  MouseClick(final int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }
}
