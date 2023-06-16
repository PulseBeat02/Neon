package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.google.common.primitives.Ints;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserDimensionCommand implements CommandSegment.Literal<CommandSender> {

  private static @NotNull final List<String> DIMENSION_SUGGESTIONS;

  static {
    DIMENSION_SUGGESTIONS =
        List.of(
            "360:640",
            "375:667",
            "414:896",
            "360:780",
            "375:812",
            "1366:768",
            "1920:1080",
            "1536:864",
            "1440:900",
            "1280:720",
            "3840:2160");
  }

  private @NotNull final Neon neon;

  private @NotNull final BrowserConfiguration config;
  private @NotNull final LiteralCommandNode<CommandSender> node;

  public BrowserDimensionCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("dimension")
            .requires(has("neon.command.browser.dimension"))
            .then(
                this.argument("dimension", StringArgumentType.greedyString())
                    .suggests(this::suggestDimension)
                    .executes(this::setDimension))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestDimension(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    DIMENSION_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  private int setDimension(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.neon.audience().sender(context.getSource());
    final Optional<int[]> optional =
        checkDimensionBoundaries(audience, context.getArgument("dimension", String.class));
    if (optional.isEmpty()) {
      return SINGLE_SUCCESS;
    }
    final int[] dims = optional.get();
    final int width = dims[0];
    final int height = dims[1];
    this.setDimensions(width, height);
    audience.sendMessage(Locale.SET_RESOLUTION.build(width, height));
    return SINGLE_SUCCESS;
  }

  public static @NotNull Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String[] dims = str.split(":");
    final Optional<Integer> width = parseInt(dims[0]);
    final Optional<Integer> height = parseInt(dims[1]);
    if (width.isPresent() && height.isPresent()) {
      return Optional.of(new int[] {width.get(), height.get()});
    }
    sender.sendMessage(Locale.INVALID_RESOLUTION.build());
    return Optional.empty();
  }

  public static @NotNull Optional<Integer> parseInt(@NotNull final String num) {
    return Optional.ofNullable(Ints.tryParse(num));
  }

  private void setDimensions(final int width, final int height) {
    this.config.setDimension(new ImmutableDimension(width, height));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
