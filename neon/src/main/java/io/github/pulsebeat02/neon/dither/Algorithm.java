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
package io.github.pulsebeat02.neon.dither;

import static io.github.pulsebeat02.neon.dither.algorithm.order.BayerMatrices.*;

import io.github.pulsebeat02.neon.dither.algorithm.error.FilterLiteDither;
import io.github.pulsebeat02.neon.dither.algorithm.error.FloydSteinbergDither;
import io.github.pulsebeat02.neon.dither.algorithm.order.BayerDither;
import io.github.pulsebeat02.neon.dither.algorithm.order.OrderedPixelMapper;
import io.github.pulsebeat02.neon.dither.algorithm.random.RandomDither;
import io.github.pulsebeat02.neon.dither.algorithm.simple.SimpleDither;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum Algorithm {

  SIMPLE(new SimpleDither()),
  FILTER_LITE(new FilterLiteDither()),
  FLOYD_STEINBERG(new FloydSteinbergDither()),
  RANDOM_LIGHT(new RandomDither(5)),
  RANDOM_HEAVY(new RandomDither(10)),
  BAYER_2X2(ordered(NORMAL_2X2, NORMAL_2X2_MAX)),
  BAYER_4X4(ordered(NORMAL_4X4, NORMAL_4X4_MAX)),
  BAYER_8X8(ordered(NORMAL_8X8, NORMAL_8X8_MAX)),
  BAYER_16X16(ordered(createBayerMatrix(16, 16), 16 * 16)),
  BAYER_32X32(ordered(createBayerMatrix(32, 32), 32 * 32)),
  BAYER_64X64(ordered(createBayerMatrix(64, 64), 64 * 64)),
  BAYER_CLUSTERED_DOT_4X4(ordered(CLUSTERED_DOT_4X4, CLUSTERED_DOT_4X4_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_8X8(
      ordered(CLUSTERED_DOT_DIAGONAL_8X8, CLUSTERED_DOT_DIAGONAL_8X8_MAX)),
  BAYER_VERTICAL_5X3(ordered(VERTICAL_5X3, VERTICAL_5X3_MAX)),
  BAYER_HORIZONTAL_3X5(ordered(HORIZONTAL_3X5, HORIZONTAL_3X5_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_6X6(
      ordered(CLUSTERED_DOT_DIAGONAL_6X6, CLUSTERED_DOT_DIAGONAL_6X6_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_8X8_2(
      ordered(CLUSTERED_DOT_DIAGONAL_8X8_2, CLUSTERED_DOT_DIAGONAL_8X8_2_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_16X16(
      ordered(CLUSTERED_DOT_DIAGONAL_16X16, CLUSTERED_DOT_DIAGONAL_16X16_MAX)),
  BAYER_CLUSTERED_DOT_6X6(ordered(CLUSTERED_DOT_6X6, CLUSTERED_DOT_6X6_MAX)),
  BAYER_CLUSTERED_DOT_SPIRAL_5X5(ordered(CLUSTERED_DOT_SPIRAL_5X5, CLUSTERED_DOT_SPIRAL_5X5_MAX)),
  BAYER_CLUSTERED_DOT_HORIZONTAL_LINE(
      ordered(CLUSTERED_DOT_HORIZONTAL_LINE, CLUSTERED_DOT_HORIZONTAL_LINE_MAX)),
  BAYER_CLUSTERED_DOT_VERTICAL_LINE(
      ordered(CLUSTERED_DOT_VERTICAL_LINE, CLUSTERED_DOT_VERTICAL_LINE_MAX)),
  BAYER_CLUSTERED_DOT_8X8(ordered(CLUSTERED_DOT_8X8, CLUSTERED_DOT_8X8_MAX)),
  BAYER_CLUSTERED_DOT_6X6_2(ordered(CLUSTERED_DOT_6X6_2, CLUSTERED_DOT_6X6_2_MAX)),
  BAYER_CLUSTERED_DOT_6X6_3(ordered(CLUSTERED_DOT_6X6_3, CLUSTERED_DOT_6X6_3_MAX)),
  BAYER_CLUSTERED_DOT_DIAGONAL_8X8_3(
      ordered(CLUSTERED_DOT_DIAGONAL_8X8_3, CLUSTERED_DOT_DIAGONAL_8X8_3_MAX));

  private static @NotNull final Map<String, Algorithm> KEY_LOOKUP;

  static {
    KEY_LOOKUP = new HashMap<>();
    for (final Algorithm setting : Algorithm.values()) {
      KEY_LOOKUP.put(setting.name(), setting);
    }
  }

  private @NotNull final DitherHandler handler;

  Algorithm(@NotNull final DitherHandler handler) {
    this.handler = handler;
  }

  private static @NotNull BayerDither ordered(final int[] @NotNull [] matrix, final int max) {
    return new BayerDither(new OrderedPixelMapper(matrix, max, 0.005f));
  }

  public static @NotNull Optional<Algorithm> ofKey(@NotNull final String key) {
    return Optional.ofNullable(KEY_LOOKUP.get(key));
  }

  public @NotNull DitherHandler getHandler() {
    return this.handler;
  }
}
