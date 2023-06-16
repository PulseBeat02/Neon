package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.ArgumentUtils.handleEmptyOptional;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.dither.Algorithm;
import io.github.pulsebeat02.neon.locale.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserDitherCommand implements CommandSegment.Literal<CommandSender> {

  private @NotNull final Neon neon;
  private @NotNull final BrowserConfiguration config;
  private @NotNull final LiteralCommandNode<CommandSender> node;

  public BrowserDitherCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("algorithm")
            .requires(has("neon.command.browser.algorithm"))
            .then(
                this.argument("algorithm", StringArgumentType.word())
                    .suggests(this::suggestDitheringAlgorithm)
                    .executes(this::setDitheringAlgorithm))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestDitheringAlgorithm(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Stream.of(Algorithm.values()).forEach(algorithm -> builder.suggest(algorithm.name()));
    return builder.buildFuture();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private int setDitheringAlgorithm(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.neon.audience().sender(context.getSource());
    final String algorithm = context.getArgument("algorithm", String.class);
    final Optional<Algorithm> setting = Algorithm.ofKey(algorithm);
    if (handleEmptyOptional(audience, Locale.INVALID_DITHER_ALGORITHM.build(algorithm), setting)) {
      return SINGLE_SUCCESS;
    }
    this.setDitheringAlgorithm(setting.get());
    audience.sendMessage(Locale.SET_DITHER_ALGORITHM.build(algorithm));
    return SINGLE_SUCCESS;
  }

  private void setDitheringAlgorithm(@NotNull final Algorithm setting) {
    this.config.setAlgorithm(setting);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
