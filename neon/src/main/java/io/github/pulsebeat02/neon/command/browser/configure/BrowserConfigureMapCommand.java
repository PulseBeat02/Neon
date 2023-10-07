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
package io.github.pulsebeat02.neon.command.browser.configure;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.github.pulsebeat02.neon.utils.ArgumentUtils.checkDimensionBoundaries;
import static io.github.pulsebeat02.neon.utils.ArgumentUtils.handleEmptyOptional;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.utils.BrowserSuggestionUtils;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.dither.Algorithm;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.Optional;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserConfigureMapCommand implements CommandSegment.Literal<CommandSender> {

  private @NotNull final Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;
  private @NotNull final BrowserConfiguration config;

  public BrowserConfigureMapCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("map")
            .requires(has("neon.command.browser.configure.map"))
            .then(
                this.argument("resolution", word())
                    .suggests(BrowserSuggestionUtils::suggestResolution)
                    .then(
                        this.argument("block-dimension", word())
                            .suggests(BrowserSuggestionUtils::suggestBlockDimension)
                            .then(
                                this.argument("algorithm", word())
                                    .suggests(BrowserSuggestionUtils::suggestDitheringAlgorithm)
                                    .executes(this::configureMapBrowser))))
            .build();
  }

  private int configureMapBrowser(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.neon.audience().sender(context.getSource());
    final String resolutionArg = context.getArgument("resolution", String.class);
    final String blockDimensionArg = context.getArgument("block-dimension", String.class);
    final String algorithmArg = context.getArgument("algorithm", String.class);

    final Optional<int[]> optionalResolution = checkDimensionBoundaries(audience, resolutionArg);
    if (optionalResolution.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final Optional<int[]> optionalBlockDimension =
        checkDimensionBoundaries(audience, blockDimensionArg);
    if (optionalBlockDimension.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final Optional<Algorithm> setting = Algorithm.ofKey(algorithmArg);
    final Component error = Locale.INVALID_DITHER_ALGORITHM.build(algorithmArg);
    if (handleEmptyOptional(audience, error, setting)) {
      return SINGLE_SUCCESS;
    }

    final int[] resolution = optionalResolution.get();
    final int[] blockDimension = optionalBlockDimension.get();
    final Algorithm algorithm = setting.get();

    this.config.setAlgorithm(algorithm);
    this.config.setResolution(new ImmutableDimension(resolution[0], resolution[1]));
    this.config.setBlockDimension(new ImmutableDimension(blockDimension[0], blockDimension[1]));

    audience.sendMessage(
        Locale.CONFIGURE_BROWSER_MAP.build(resolutionArg, blockDimensionArg, algorithmArg));

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
