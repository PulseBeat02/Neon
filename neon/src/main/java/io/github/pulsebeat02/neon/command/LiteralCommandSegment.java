package io.github.pulsebeat02.neon;

import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

public interface LiteralCommandSegment<S> extends CommandSegment<S, LiteralCommandNode<S>> {

  @Override
  @NotNull
  LiteralCommandNode<S> getNode();
}
