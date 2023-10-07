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

import com.google.common.primitives.Ints;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.locale.Locale;
import java.util.Optional;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArgumentUtils {

  private ArgumentUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static boolean requiresPlayer(
      @NotNull final Neon plugin, @NotNull final CommandSender sender) {
    return handleFalse(
        plugin.audience().sender(sender), Locale.ERR_SENDER.build(), sender instanceof Player);
  }

  public static <T> boolean handleEmptyOptional(
      @NotNull final Audience audience,
      @NotNull final Component component,
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NotNull final Optional<T> optional) {
    if (optional.isEmpty()) {
      audience.sendMessage(component);
      return true;
    }
    return false;
  }

  public static boolean handleNull(
      @NotNull final Audience audience,
      @NotNull final Component component,
      @Nullable final Object obj) {
    return handleTrue(audience, component, obj == null);
  }

  public static boolean handleNonNull(
      @NotNull final Audience audience,
      @NotNull final Component component,
      @Nullable final Object obj) {
    return handleFalse(audience, component, obj == null);
  }

  public static boolean handleFalse(
      @NotNull final Audience audience,
      @NotNull final Component component,
      final boolean statement) {
    if (!statement) {
      audience.sendMessage(component);
      return true;
    }
    return false;
  }

  public static boolean handleTrue(
      @NotNull final Audience audience,
      @NotNull final Component component,
      final boolean statement) {
    if (statement) {
      audience.sendMessage(component);
      return true;
    }
    return false;
  }

  public static @NotNull Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {

    final String[] dims = str.split("x");
    if (dims.length != 2) {
      sender.sendMessage(Locale.ERR_RES.build());
      return Optional.empty();
    }

    final Optional<Integer> width = parseInt(dims[0]);
    final Optional<Integer> height = parseInt(dims[1]);
    if (width.isPresent() && height.isPresent()) {
      return Optional.of(new int[] {width.get(), height.get()});
    }

    sender.sendMessage(Locale.ERR_RES.build());

    return Optional.empty();
  }

  public static @NotNull Optional<Integer> parseInt(@NotNull final String num) {
    return Optional.ofNullable(Ints.tryParse(num));
  }
}
