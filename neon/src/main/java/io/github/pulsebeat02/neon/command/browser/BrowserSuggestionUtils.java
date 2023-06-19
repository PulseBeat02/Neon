package io.github.pulsebeat02.neon.command.browser;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.pulsebeat02.neon.command.browser.configure.EntitySelection;
import io.github.pulsebeat02.neon.dither.Algorithm;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserSuggestionUtils {

  private static @NotNull final List<String> RESOLUTION_SUGGESTIONS;
  private static @NotNull final List<String> BLOCK_DIMENSION_SUGGESTIONS;
  private static @NotNull final List<String> CHARACTER_SUGGESTIONS;
  private static @NotNull final List<String> BROWSER_RENDER_TYPE_SUGGESTIONS;

  static {
    RESOLUTION_SUGGESTIONS =
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
    BLOCK_DIMENSION_SUGGESTIONS =
        List.of("1:1", "1:2", "3:3", "3:5", "5:5", "6:10", "8:14", "10:14", "8:18");
    CHARACTER_SUGGESTIONS = List.of("■", "■", "█", "▬", ":)");
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
    CHARACTER_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  public static @NotNull CompletableFuture<Suggestions> suggestRenderTypes(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    BROWSER_RENDER_TYPE_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }
}
