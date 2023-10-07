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
            .then(new BrowserDestroyCommand(neon).getNode())
            .build();
  }

  @Override
  public @NotNull Component usage() {
    return LocaleParent.getCommandUsageComponent(
        Map.of(
            "/browser load [entity|map] [url]",
            "Starts the browser",
            "/browser configure map [width:height] [blockWidth:blockHeight] [algorithm]",
            "Configures a browser using map callbacks",
            "/browser configure entity [width:height] [display type] [character]",
            "Configures a browser using entity callbacks",
            "/browser destroy",
            "Destroys the browser"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}