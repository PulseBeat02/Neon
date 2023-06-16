package io.github.pulsebeat02.neon;

import static java.util.Objects.requireNonNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.pulsebeat02.neon.command.BaseCommand;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandHandler implements TabExecutor {

  private final CommandDispatcher<CommandSender> dispatcher;
  private final RootCommandNode<CommandSender> rootNode;
  private final Set<BaseCommand> commands;
  private final Neon neon;

  public CommandHandler(@NotNull final Neon neon) {
    this.neon = neon;
    this.dispatcher = new CommandDispatcher<>();
    this.rootNode = this.dispatcher.getRoot();
    this.commands = this.getPluginCommands();
    this.registerCommands();
  }

  @Contract(" -> new")
  private @NotNull @Unmodifiable Set<BaseCommand> getPluginCommands() {
    return Set.of();
  }

  private void registerCommands() {
    final Commodore commodore =
        CommodoreProvider.isSupported() ? CommodoreProvider.getCommodore(neon) : null;
    this.commands.forEach(command -> this.registerProperCommand(command, commodore));
  }

  private void registerProperCommand(
      @NotNull final BaseCommand command, @Nullable final Commodore commodore) {

    final CommandMap commandMap = CommandMapHelper.getCommandMap();
    this.rootNode.addChild(command.getNode());
    commandMap.register(this.neon.getBootstrap().getName(), command);

    this.registerCommodoreCommand(commodore, command);
  }

  private void registerCommodoreCommand(
      @Nullable final Commodore commodore, @NotNull final BaseCommand command) {
    Nill.ifNot(commodore, () -> this.parseFile(commodore, command));
  }

  private void parseFile(@NotNull final Commodore commodore, @NotNull final BaseCommand command) {
    try {
      final InputStream resource =
          this.neon
              .getBootstrap()
              .getResource("commodore/%s.commodore".formatted(command.getName()));
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
  public List<String> onTabComplete(
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

  @NotNull
  public CommandDispatcher<CommandSender> getDispatcher() {
    return this.dispatcher;
  }

  @NotNull
  public RootCommandNode<CommandSender> getRootNode() {
    return this.rootNode;
  }

  @NotNull
  public Set<BaseCommand> getCommands() {
    return this.commands;
  }
}
