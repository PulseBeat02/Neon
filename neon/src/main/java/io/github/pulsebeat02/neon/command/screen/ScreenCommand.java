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
package io.github.pulsebeat02.neon.command.screen;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.utils.ArgumentUtils.requiresPlayer;

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
    new ScreenBuilderGui(plugin, (Player) sender);
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
