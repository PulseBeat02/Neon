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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.neon.json.GsonProvider;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class MapPalette {

  public static final Color[] NMS_PALETTE;

  static {
    final Gson gson = GsonProvider.getGson();
    try (final Reader reader = ResourceUtils.getResourceAsReader("colors/colors.json")) {
      final TypeToken<int[][]> token = new TypeToken<>() {};
      final int[][] colors = gson.fromJson(reader, token.getType());
      NMS_PALETTE =
          Stream.of(colors)
              .map(color -> new Color(color[0], color[1], color[2]))
              .toArray(Color[]::new);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void init() {}

  @NotNull
  public static Color getColor(final byte val) {
    return NMS_PALETTE[val];
  }
}
