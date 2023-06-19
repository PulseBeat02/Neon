package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.locale.Locale;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserDestroyCommand implements CommandSegment.Literal<CommandSender> {

  private final @NotNull Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;

  public BrowserDestroyCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.node =
        this.literal("destroy")
            .requires(has("neon.command.browser.destroy"))
            .executes(this::startBrowser)
            .build();
  }

  private int startBrowser(@NotNull final CommandContext<CommandSender> context) {

    this.neon.shutdownBrowser();

    final Audience audience = this.neon.audience().sender(context.getSource());
    audience.sendMessage(Locale.DESTROY_BROWSER.build());

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
