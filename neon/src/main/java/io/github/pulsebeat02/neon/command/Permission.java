package io.github.pulsebeat02.neon.command;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Permission implements Predicate<CommandSender> {

  private @NotNull final Predicate<CommandSender> delegate;

  Permission(@NotNull Predicate<CommandSender> delegate) {
    while (delegate instanceof Permission) {
      delegate = ((Permission) delegate).delegate;
    }
    this.delegate = delegate;
  }

  public static @NotNull Permission has(@NotNull final String permission) {
    requireNonNull(permission, "permission");
    final String lowercase = permission.toLowerCase(Locale.ROOT);
    return new Permission(subject -> subject.hasPermission(lowercase));
  }

  public static @NotNull Permission lacks(@NotNull final String permission) {
    return has(permission).negate();
  }

  @Override
  public boolean test(@NotNull final CommandSender subject) {
    return this.delegate.test(subject);
  }

  @Override
  public @NotNull Permission and(@NotNull final Predicate<? super CommandSender> other) {
    return new Permission(this.delegate.and(requireNonNull(other, "other")));
  }

  public @NotNull Permission and(@NotNull final String other) {
    return this.and(has(other));
  }

  @Override
  public @NotNull Permission or(@NotNull final Predicate<? super CommandSender> other) {
    return new Permission(this.delegate.or(requireNonNull(other, "other")));
  }

  public @NotNull Permission or(@NotNull final String other) {
    return new Permission(this.or(has(other)));
  }

  @Override
  public @NotNull Permission negate() {
    return new Permission(this.delegate.negate());
  }
}
