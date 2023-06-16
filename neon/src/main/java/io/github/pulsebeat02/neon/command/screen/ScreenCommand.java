package io.github.pulsebeat02.neon.command.screen;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.ArgumentUtils.requiresPlayer;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.BaseCommand;
import io.github.pulsebeat02.neon.locale.LocaleParent;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ScreenCommand extends BaseCommand {

  private @NotNull final LiteralCommandNode<CommandSender> node;

  public ScreenCommand(@NotNull final Neon neon, @NotNull final TabExecutor executor) {
    super(neon, "screen", executor, "neon.command.screen", "");
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .executes(this::sendScreenBuilder)
            .build();
  }

  private int sendScreenBuilder(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Neon plugin = this.plugin();
    if (requiresPlayer(plugin, sender)) {
      return SINGLE_SUCCESS;
    }
    new ScreenBuilderGui((Player) sender);
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull Component usage() {
    return LocaleParent.getCommandUsageComponent(
        Map.of("/screen", "Opens the screen building GUI"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
