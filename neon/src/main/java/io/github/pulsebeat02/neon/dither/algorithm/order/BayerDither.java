package io.github.pulsebeat02.neon.dither.algorithm.order;

import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;

public class BayerDither implements DitherHandler {

  private final float[][] precalc;
  private final int xdim;
  private final int ydim;

  public BayerDither(@NotNull final OrderedPixelMapper mapper) {
    this.precalc = mapper.getMatrix();
    this.ydim = this.precalc.length;
    this.xdim = this.precalc[0].length;
  }

  @Override
  public @NotNull ByteBuf dither(@NotNull final ByteBuf buffer, final int width) {
    final int length = buffer.capacity();
    final int height = length / width;
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer.getByte(index);
        final int b1 = color >> 24 & 0xFF;
        final int g1 = color >> 16 & 0xFF;
        final int r1 = color >> 8 & 0xFF;
        final int a1 = color & 0xFF;
        int r = (1 - a1) * 255 + a1 * b1;
        int g = (1 - a1) * 255 + a1 * g1;
        int b = (1 - a1) * 255 + a1 * r1;
        r = (r += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(b, 0);
        data.writeByte(DitherUtils.getBestColor(r, g, b));
      }
    }
    return data;
  }
}
