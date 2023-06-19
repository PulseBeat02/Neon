package io.github.pulsebeat02.neon.browser.widgets;

import java.nio.IntBuffer;
import org.jetbrains.annotations.NotNull;

public final class BrowserWidget {

  private @NotNull final IntBuffer buffer;
  private final int x;
  private final int y;

  public BrowserWidget(@NotNull final IntBuffer buffer, final int x, final int y) {
    this.buffer = buffer;
    this.x = x;
    this.y = y;
  }

  public @NotNull IntBuffer getBuffer() {
    return this.buffer;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }
}
