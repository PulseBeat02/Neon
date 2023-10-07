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

  private @NotNull final Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;

  public BrowserDestroyCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.node =
        this.literal("destroy")
            .requires(has("neon.command.browser.info.destroy"))
            .executes(this::startBrowser)
            .build();
  }

  private int startBrowser(@NotNull final CommandContext<CommandSender> context) {

    this.neon.shutdownBrowser();

    final Audience audience = this.neon.audience().sender(context.getSource());
    audience.sendMessage(Locale.INFO_DESTROY_BROWSER.build());

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
