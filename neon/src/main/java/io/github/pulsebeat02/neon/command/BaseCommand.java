package io.github.pulsebeat02.neon.command;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.Neon;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand extends Command implements LiteralCommandSegment<CommandSender> {

  private @NotNull final TabExecutor executor;
  private @NotNull final Neon neon;
  private @NotNull final BukkitAudiences audience;

  public BaseCommand(
      @NotNull final Neon neon,
      @NotNull final String name,
      @NotNull final TabExecutor executor,
      @NotNull final String permission,
      @NotNull final String... aliases) {
    super(name);
    this.setPermission(permission);
    this.setAliases(Arrays.asList(aliases));
    this.neon = neon;
    this.executor = executor;
    this.audience = neon.audience();
  }

  public @NotNull abstract Component usage();

  @Override
  public boolean execute(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return this.executor.onCommand(sender, this, label, args);
  }

  @Override
  public @NotNull List<String> tabComplete(
      @NotNull final CommandSender sender, @NotNull final String label, final String... args) {
    return requireNonNull(this.executor.onTabComplete(sender, this, label, args));
  }

  public @NotNull Neon plugin() {
    return this.neon;
  }

  public @NotNull BukkitAudiences audience() {
    return this.audience;
  }
}
