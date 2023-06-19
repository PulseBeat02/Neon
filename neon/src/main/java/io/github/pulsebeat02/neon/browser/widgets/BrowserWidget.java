package io.github.pulsebeat02.neon.browser.widgets;

import java.nio.IntBuffer;

public final class BrowserWidget {

  private final IntBuffer buffer;
  private final int x;
  private final int y;

  public BrowserWidget(final IntBuffer buffer, final int x, final int y) {
    this.buffer = buffer;
    this.x = x;
    this.y = y;
  }

  public IntBuffer getBuffer() {
    return this.buffer;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }
}
