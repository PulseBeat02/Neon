/*
 * MIT License
 *
 * Copyright (c) 2024 Brandon Li
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
package io.github.pulsebeat02.neon.dither.algorithm.error;

import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.github.pulsebeat02.neon.utils.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jetbrains.annotations.NotNull;

public class FloydSteinbergDither implements DitherHandler {

  @Override
  public @NotNull ByteBuf dither(@NotNull final IntBuffer buffer, final int width) {
    final int length = buffer.capacity();
    final int height = length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; x++) {
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer.get(index);
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : Math.max(red, 0);
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : Math.max(green, 0);
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : Math.max(blue, 0);
          final int closest = DitherUtils.getBestFullColor(red, green, blue);
          final int r = closest >> 16 & 0xFF;
          final int g = closest >> 8 & 0xFF;
          final int b = closest & 0xFF;
          final int delta_r = red - r;
          final int delta_g = green - g;
          final int delta_b = blue - b;
          if (hasNextX) {
            buf1[bufferIndex] = (delta_r >> 4) * 7;
            buf1[bufferIndex + 1] = (delta_g >> 4) * 7;
            buf1[bufferIndex + 2] = (delta_b >> 4) * 7;
          }
          if (hasNextY) {
            if (x > 0) {
              buf2[bufferIndex - 6] = (delta_r >> 4) * 3;
              buf2[bufferIndex - 5] = (delta_g >> 4) * 3;
              buf2[bufferIndex - 4] = (delta_b >> 4) * 3;
            }
            buf2[bufferIndex - 3] = (delta_r >> 4) * 5;
            buf2[bufferIndex - 2] = (delta_g >> 4) * 5;
            buf2[bufferIndex - 1] = (delta_b >> 4) * 5;
            if (hasNextX) {
              buf2[bufferIndex] = delta_r >> 4;
              buf2[bufferIndex + 1] = delta_g >> 4;
              buf2[bufferIndex + 2] = delta_b >> 4;
            }
          }
          data.setByte(index, DitherUtils.getBestColor(r, g, b));
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer.get(index);
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : Math.max(blue, 0);
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : Math.max(green, 0);
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : Math.max(red, 0);
          final int closest = DitherUtils.getBestFullColor(red, green, blue);
          final int r = closest >> 16 & 0xFF;
          final int g = closest >> 8 & 0xFF;
          final int b = closest & 0xFF;
          final int delta_r = red - r;
          final int delta_g = green - g;
          final int delta_b = blue - b;
          if (hasNextX) {
            buf1[bufferIndex] = (delta_b >> 4) * 7;
            buf1[bufferIndex - 1] = (delta_g >> 4) * 7;
            buf1[bufferIndex - 2] = (delta_r >> 4) * 7;
          }
          if (hasNextY) {
            if (x < widthMinus) {
              buf2[bufferIndex + 6] = (delta_b >> 4) * 3;
              buf2[bufferIndex + 5] = (delta_g >> 4) * 3;
              buf2[bufferIndex + 4] = (delta_r >> 4) * 3;
            }
            buf2[bufferIndex + 3] = (delta_b >> 4) * 5;
            buf2[bufferIndex + 2] = (delta_g >> 4) * 5;
            buf2[bufferIndex + 1] = (delta_r >> 4) * 5;
            if (hasNextX) {
              buf2[bufferIndex] = delta_b >> 4;
              buf2[bufferIndex - 1] = delta_g >> 4;
              buf2[bufferIndex - 2] = delta_r >> 4;
            }
          }
          data.setByte(index, DitherUtils.getBestColor(r, g, b));
        }
      }
    }
    return data;
  }
}
