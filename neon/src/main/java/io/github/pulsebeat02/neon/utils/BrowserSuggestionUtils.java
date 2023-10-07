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
package io.github.pulsebeat02.neon.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.pulsebeat02.neon.command.browser.configure.EntitySelection;
import io.github.pulsebeat02.neon.dither.Algorithm;

import java.io.IOException;
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
    final String parent = "command/suggestions";
    try {
      RESOLUTION_SUGGESTIONS = JsonUtils.toListFromResource("%s/resolution.json".formatted(parent));
      BLOCK_DIMENSION_SUGGESTIONS =
          JsonUtils.toListFromResource("%s/blockdim.json".formatted(parent));
      BROWSER_RENDER_TYPE_SUGGESTIONS =
          JsonUtils.toListFromResource("%s/rendertype.json".formatted(parent));
      CHARACTER_SUGGESTIONS = JsonUtils.toMapFromResource("%s/character.json".formatted(parent));
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static void init() {}

  private BrowserSuggestionUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
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
