package io.github.pulsebeat02.neon.dither.load;

import java.io.Serial;
import java.util.concurrent.RecursiveTask;

final class LoadBlue extends RecursiveTask<Byte> {

  @Serial private static final long serialVersionUID = 5331764784578439634L;
  private final int r, g, b;
  private final int[] palette;

  LoadBlue(final int[] palette, final int r, final int g, final int b) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.palette = palette;
  }

  @Override
  protected Byte compute() {
    int val = 0;
    float best_distance = Float.MAX_VALUE;
    float distance;
    int col;
    final float f = 1 / 256F;
    for (int i = 4; i < this.palette.length; ++i) {
      col = this.palette[i];
      distance = this.calculateDistance(col, f);
      if (distance < best_distance) {
        best_distance = distance;
        val = i;
      }
    }
    return (byte) val;
  }

  private float calculateDistance(final int col, final float f) {
    final int r2 = col >> 16 & 0xFF;
    final int g2 = col >> 8 & 0xFF;
    final int b2 = col & 0xFF;
    final float red_avg = (this.r + r2) * 0.5f;
    final int redVal = this.r - r2;
    final int greenVal = this.g - g2;
    final int blueVal = this.b - b2;
    final float weight_red = 2.0f + red_avg * f;
    final float weight_green = 4.0f;
    final float weight_blue = 2.0f + (255.0f - red_avg) * f;
    return weight_red * redVal * redVal
        + weight_green * greenVal * greenVal
        + weight_blue * blueVal * blueVal;
  }
}
