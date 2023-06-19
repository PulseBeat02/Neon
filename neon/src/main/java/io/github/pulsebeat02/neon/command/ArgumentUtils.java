package io.github.pulsebeat02.neon.command;

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
        plugin.audience().sender(sender), Locale.INVALID_SENDER.build(), sender instanceof Player);
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
      sender.sendMessage(Locale.INVALID_RESOLUTION.build());
      return Optional.empty();
    }
    final Optional<Integer> width = parseInt(dims[0]);
    final Optional<Integer> height = parseInt(dims[1]);
    if (width.isPresent() && height.isPresent()) {
      return Optional.of(new int[] {width.get(), height.get()});
    }
    sender.sendMessage(Locale.INVALID_RESOLUTION.build());
    return Optional.empty();
  }

  public static @NotNull Optional<Integer> parseInt(@NotNull final String num) {
    return Optional.ofNullable(Ints.tryParse(num));
  }
}
