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
package io.github.pulsebeat02.neon.command;

import static java.util.Objects.requireNonNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.browser.BrowserCommand;
import io.github.pulsebeat02.neon.command.screen.ScreenCommand;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileReader;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandHandler implements TabExecutor {

  private @NotNull final CommandDispatcher<CommandSender> dispatcher;
  private @NotNull final RootCommandNode<CommandSender> rootNode;
  private @NotNull final Set<BaseCommand> commands;
  private @NotNull final Neon neon;

  public CommandHandler(@NotNull final Neon neon) {
    this.neon = neon;
    this.dispatcher = new CommandDispatcher<>();
    this.rootNode = this.dispatcher.getRoot();
    this.commands = this.getPluginCommands();
    this.registerCommands();
  }

  private @NotNull @Unmodifiable Set<BaseCommand> getPluginCommands() {
    return Set.of(new ScreenCommand(this.neon, this), new BrowserCommand(this.neon, this));
  }

  private void registerCommands() {
    final Commodore commodore =
        CommodoreProvider.isSupported() ? CommodoreProvider.getCommodore(this.neon) : null;
    this.commands.forEach(command -> this.registerProperCommand(command, commodore));
  }

  private void registerProperCommand(
      @NotNull final BaseCommand command, @Nullable final Commodore commodore) {
    final CommandMap commandMap = CommandMapHelper.getCommandMap();
    this.rootNode.addChild(command.getNode());
    commandMap.register(this.neon.getName(), command);
    this.registerCommodoreCommand(commodore, command);
  }

  private void registerCommodoreCommand(
      @Nullable final Commodore commodore, @NotNull final BaseCommand command) {
    if (commodore != null) {
      this.parseFile(commodore, command);
    }
  }

  private void parseFile(@NotNull final Commodore commodore, @NotNull final BaseCommand command) {
    try {
      final InputStream resource =
          this.neon.getResource("command/%s.commodore".formatted(command.getName()));
      commodore.register(CommodoreFileReader.INSTANCE.parse(requireNonNull(resource)));
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public boolean onCommand(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String label,
      final String @NotNull [] args) {
    final Audience audience = this.neon.audience().sender(sender);
    try {
      this.dispatcher.execute(this.parsedCommand(sender, command, args, true));
    } catch (final CommandSyntaxException exception) {
      audience.sendMessage(((BaseCommand) command).usage());
    }
    return true;
  }

  @Override
  public @NotNull List<String> onTabComplete(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String alias,
      final String @NotNull [] args) {
    return this.dispatcher
        .getCompletionSuggestions(this.parsedCommand(sender, command, args, false))
        .join()
        .getList()
        .stream()
        .map(Suggestion::getText)
        .collect(Collectors.toList());
  }

  private @NotNull ParseResults<CommandSender> parsedCommand(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      final String @NotNull [] args,
      final boolean trim) {
    String cmd = "%s %s".formatted(command.getName(), String.join(" ", args));
    if (trim) {
      cmd = cmd.trim();
    }
    return this.dispatcher.parse(cmd, sender);
  }

  public @NotNull CommandDispatcher<CommandSender> getDispatcher() {
    return this.dispatcher;
  }

  public @NotNull RootCommandNode<CommandSender> getRootNode() {
    return this.rootNode;
  }

  public @NotNull Set<BaseCommand> getCommands() {
    return this.commands;
  }
}
