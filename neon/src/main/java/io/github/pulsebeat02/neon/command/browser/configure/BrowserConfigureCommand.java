package io.github.pulsebeat02.neon.command.browser.configure;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserConfigureCommand implements CommandSegment.Literal<CommandSender> {
  private @NotNull final LiteralCommandNode<CommandSender> node;

  public BrowserConfigureCommand(@NotNull final Neon neon) {
    this.node =
        this.literal("configure")
            .then(new BrowserConfigureEntityCommand(neon).getNode())
            .then(new BrowserConfigureMapCommand(neon).getNode())
            .build();
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
