/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static io.github.pulsebeat02.neon.utils.ArgumentUtils.*;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.utils.BrowserSuggestionUtils;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.Optional;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BrowserConfigureEntityCommand implements CommandSegment.Literal<CommandSender> {

  private @NotNull final Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;
  private @NotNull final BrowserConfiguration config;

  public BrowserConfigureEntityCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("entity")
            .requires(has("neon.command.browser.configure.entity"))
            .then(
                this.argument("resolution", word())
                    .suggests(BrowserSuggestionUtils::suggestResolution)
                    .then(
                        this.argument("entity", word())
                            .suggests(BrowserSuggestionUtils::suggestEntityDisplay)
                            .then(
                                this.argument("character", string())
                                    .suggests(BrowserSuggestionUtils::suggestCharacter)
                                    .executes(this::configureEntityBrowser))))
            .build();
  }

  private int configureEntityBrowser(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    if (requiresPlayer(this.neon, sender)) {
      return SINGLE_SUCCESS;
    }

    final Player player = (Player) sender;
    final Audience audience = this.neon.audience().sender(sender);
    final String resolutionArg = context.getArgument("resolution", String.class);
    final String entityArg = context.getArgument("entity", String.class);
    final String characterArg = context.getArgument("character", String.class);

    final Optional<int[]> optionalResolution = checkDimensionBoundaries(audience, resolutionArg);
    if (optionalResolution.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final Optional<EntitySelection> optionalEntity = EntitySelection.ofKey(entityArg);
    final Component error = Locale.INVALID_ENTITY_SELECTION.build();
    if (handleEmptyOptional(audience, error, optionalEntity)) {
      return SINGLE_SUCCESS;
    }

    final Optional<String> optionalCharacter = BrowserSuggestionUtils.getCharacter(characterArg);
    final String character = optionalCharacter.orElse(characterArg);
    final int[] resolution = optionalResolution.get();
    final EntitySelection entity = optionalEntity.get();
    this.config.setResolution(new ImmutableDimension(resolution[0], resolution[1]));
    this.config.setEntitySelection(entity);
    this.config.setCharacter(character);
    this.config.setLocation(player.getLocation());

    audience.sendMessage(
        Locale.CONFIGURE_BROWSER_ENTITY.build(resolutionArg, entityArg, character));

    return SINGLE_SUCCESS;
  }

  @NotNull
  @Override
  public LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
