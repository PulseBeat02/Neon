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
package io.github.pulsebeat02.neon.utils;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.jetbrains.annotations.NotNull;

public final class ResourceUtils {

  private ResourceUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static @NotNull InputStream getResourceAsStream(@NotNull final String name) {
    return requireNonNull(ResourceUtils.class.getClassLoader().getResourceAsStream(name));
  }

  public static @NotNull Reader getResourceAsReader(@NotNull final String name) {
    return new BufferedReader(new InputStreamReader(getResourceAsStream(name)));
  }

  public static @NotNull String getFilename(@NotNull final String url) {
    return url.substring(url.lastIndexOf('/') + 1);
  }
}
