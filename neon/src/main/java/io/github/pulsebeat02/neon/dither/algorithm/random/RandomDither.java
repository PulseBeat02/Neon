package io.github.pulsebeat02.neon.dither.algorithm.random;

import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.IntBuffer;
import java.util.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;

public final class RandomDither implements DitherHandler {

  public static final int LIGHT_WEIGHT;
  public static final int NORMAL_WEIGHT;
  public static final int HEAVY_WEIGHT;
  private static final RandomGenerator RANDOM;

  static {
    RANDOM = new Xoroshiro128PlusRandom();
    LIGHT_WEIGHT = 32;
    NORMAL_WEIGHT = 64;
    HEAVY_WEIGHT = 128;
  }

  private final int min;
  private final int max;

  public RandomDither(final int weight) {
    this.min = -weight;
    this.max = weight + 1;
  }

  @Override
  public @NotNull ByteBuf dither(@NotNull final IntBuffer buffer, final int width) {
    final int length = buffer.capacity();
    final int height = length / width;
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer.get(index);
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        r = (r += this.random()) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.random()) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.random()) > 255 ? 255 : Math.max(b, 0);
        data.setByte(index, DitherUtils.getBestColor(r, g, b));
      }
    }
    return data;
  }

  private int random() {
    return RANDOM.nextInt(this.min, this.max);
  }
}
