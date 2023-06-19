package io.github.pulsebeat02.neon.command.browser;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.BaseCommand;
import io.github.pulsebeat02.neon.command.browser.configure.BrowserConfigureCommand;
import io.github.pulsebeat02.neon.locale.LocaleParent;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public final class BrowserCommand extends BaseCommand {

  private @NotNull final LiteralCommandNode<CommandSender> node;

  public BrowserCommand(@NotNull final Neon neon, @NotNull final TabExecutor executor) {
    super(neon, "browser", executor, "neon.command.browser", "");
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(new BrowserLoadCommand(neon).getNode())
            .then(new BrowserConfigureCommand(neon).getNode())
            .build();
  }

  @Override
  public @NotNull Component usage() {
    return LocaleParent.getCommandUsageComponent(
        Map.of(
            "/browser load",
            "Starts the browser",
            "/browser configure map [width:height] [blockWidth:blockHeight] [algorithm]",
            "Configures a browser using map callbacks",
            "/browser configure entity [width:height] [display type] [character]",
            "Configures a browser using entity callbacks"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
