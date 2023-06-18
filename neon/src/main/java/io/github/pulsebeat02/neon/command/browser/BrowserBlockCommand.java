package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.Permission.has;
import static io.github.pulsebeat02.neon.command.browser.BrowserResolutionCommand.checkDimensionBoundaries;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserBlockCommand implements CommandSegment.Literal<CommandSender> {

  private static @NotNull final List<String> DIMENSION_SUGGESTIONS;

  static {
    DIMENSION_SUGGESTIONS =
        List.of("1:1", "1:2", "3:3", "3:5", "5:5", "6:10", "8:14", "10:14", "8:18");
  }

  private @NotNull final Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;
  private @NotNull final BrowserConfiguration config;

  public BrowserBlockCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("block-dimension")
            .requires(has("neon.command.browser.block-dimension"))
            .then(
                this.argument("block-dimension", StringArgumentType.greedyString())
                    .suggests(this::suggestBlockDimension)
                    .executes(this::setBlockDimension))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestBlockDimension(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    DIMENSION_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  private int setBlockDimension(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.neon.audience().sender(context.getSource());
    final Optional<int[]> optional =
        checkDimensionBoundaries(audience, context.getArgument("block-dimension", String.class));
    if (optional.isEmpty()) {
      return SINGLE_SUCCESS;
    }
    final int[] dims = optional.get();
    final int width = dims[0];
    final int height = dims[1];
    this.setDimensions(width, height);
    audience.sendMessage(Locale.SET_BLOCK_DIMENSION.build(width, height));
    return SINGLE_SUCCESS;
  }

  private void setDimensions(final int width, final int height) {
    this.config.setBlockWidth(width);
    this.config.setBlockHeight(height);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
