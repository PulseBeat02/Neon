package io.github.pulsebeat02.neon.command.browser;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.pulsebeat02.neon.command.browser.configure.EntitySelection;
import io.github.pulsebeat02.neon.dither.Algorithm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserSuggestionUtils {

  private static @NotNull final List<String> RESOLUTION_SUGGESTIONS;
  private static @NotNull final List<String> BLOCK_DIMENSION_SUGGESTIONS;
  private static @NotNull final List<String> BROWSER_RENDER_TYPE_SUGGESTIONS;
  private static @NotNull final Map<String, String> CHARACTER_SUGGESTIONS;

  static {
    RESOLUTION_SUGGESTIONS =
        List.of(
            "360x640",
            "375x667",
            "414x896",
            "360x780",
            "375x812",
            "1366x768",
            "1920x1080",
            "1536x864",
            "1440x900",
            "1280x720",
            "3840x2160");
    BLOCK_DIMENSION_SUGGESTIONS =
        List.of("1x1", "1x2", "3x3", "3x5", "5x5", "6x10", "8x14", "10x14", "8x8");
    CHARACTER_SUGGESTIONS =
        Map.of(
            "SQUARE",
            "■",
            "VERTICAL_RECTANGLE",
            "█",
            "HORIZONTAL_RECTANGLE",
            "▬",
            "ROUNDED_SQUARE",
            "▢",
            "SMILEY",
            "\uD83D\uDE0A");
    BROWSER_RENDER_TYPE_SUGGESTIONS = List.of("MAP", "ENTITY");
  }

  public static @NotNull CompletableFuture<Suggestions> suggestResolution(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    RESOLUTION_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  public static @NotNull CompletableFuture<Suggestions> suggestBlockDimension(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    BLOCK_DIMENSION_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  public static @NotNull CompletableFuture<Suggestions> suggestDitheringAlgorithm(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Stream.of(Algorithm.values()).forEach(algorithm -> builder.suggest(algorithm.name()));
    return builder.buildFuture();
  }

  public static @NotNull CompletableFuture<Suggestions> suggestEntityDisplay(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Stream.of(EntitySelection.values()).forEach(selection -> builder.suggest(selection.name()));
    return builder.buildFuture();
  }

  public static @NotNull CompletableFuture<Suggestions> suggestCharacter(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    CHARACTER_SUGGESTIONS.keySet().forEach(builder::suggest);
    return builder.buildFuture();
  }

  public static @NotNull CompletableFuture<Suggestions> suggestRenderTypes(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    BROWSER_RENDER_TYPE_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  public static @NotNull Optional<String> getCharacter(@NotNull final String key) {
    return Optional.ofNullable(CHARACTER_SUGGESTIONS.get(key));
  }
}
