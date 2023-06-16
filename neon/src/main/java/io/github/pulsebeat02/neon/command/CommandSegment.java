package io.github.pulsebeat02.neon.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandSegment<S, N extends CommandNode<S>> {

  @NotNull
  N getNode();

  @NotNull
  default LiteralArgumentBuilder<S> literal(@NotNull final String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  @NotNull
  default <T> RequiredArgumentBuilder<S, T> argument(
      @NotNull final String name, @NotNull final ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  @FunctionalInterface
  interface Root<S> extends CommandSegment<S, RootCommandNode<S>> {}

  @FunctionalInterface
  interface Literal<S> extends CommandSegment<S, LiteralCommandNode<S>> {}

  @FunctionalInterface
  interface Argument<S, T> extends CommandSegment<S, ArgumentCommandNode<S, T>> {}
}
