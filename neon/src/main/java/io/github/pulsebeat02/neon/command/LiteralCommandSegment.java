package io.github.pulsebeat02.neon.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

public interface LiteralCommandSegment<S> extends CommandSegment<S, LiteralCommandNode<S>> {

  @Override
  @NotNull
  LiteralCommandNode<S> getNode();
}
