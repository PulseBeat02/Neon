/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.neon.dither.algorithm.order;

import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
  public @NotNull ByteBuf dither(@NotNull final IntBuffer buffer, final int width) {
    final int length = buffer.capacity();
    final int height = length / width;
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int rgb = buffer.get(index);
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        r = (r += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(b, 0);
        data.writeByte(DitherUtils.getBestColor(r, g, b));
      }
    }
    return data;
  }
}
