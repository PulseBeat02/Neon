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
package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.SeleniumBrowser;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.utils.ArgumentUtils;
import io.github.pulsebeat02.neon.utils.BrowserSuggestionUtils;
import io.github.pulsebeat02.neon.video.EntityRenderMethod;
import io.github.pulsebeat02.neon.video.HologramRenderMethod;
import io.github.pulsebeat02.neon.video.MapRenderMethod;
import io.github.pulsebeat02.neon.video.ParticleRenderMethod;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class BrowserLoadCommand implements CommandSegment.Literal<CommandSender> {

  private @NotNull final Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;
  private @NotNull final BrowserConfiguration config;

  public BrowserLoadCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("load")
            .requires(has("neon.command.browser.info.load"))
            .then(
                this.argument("type", word())
                    .suggests(BrowserSuggestionUtils::suggestRenderTypes)
                    .then(this.argument("url", greedyString()).executes(this::startBrowser)))
            .build();
  }

  private int startBrowser(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.neon.audience().sender(context.getSource());
    final String type = context.getArgument("type", String.class).toLowerCase();
    final String url = context.getArgument("url", String.class);
    final BrowserSettings settings = BrowserSettings.ofSettings(this.config);
    final RenderMethod method =
        switch (type) {
          case "map" -> new MapRenderMethod(this.neon, settings);
          case "entity" -> switch (this.config.getSelection()) {
            case HOLOGRAM -> new HologramRenderMethod(this.neon, settings);
            case PARTICLE -> new ParticleRenderMethod(this.neon, settings);
          };
          default -> null;
        };

    final Component browserError = Locale.ERR_BROWSER.build();
    if (ArgumentUtils.handleNull(audience, browserError, method)) {
      return SINGLE_SUCCESS;
    }

    final Component homepageError = Locale.ERR_URL.build();
    if (ArgumentUtils.handleFalse(audience, homepageError, this.checkUrl(url))) {
      return SINGLE_SUCCESS;
    }

    if (method instanceof EntityRenderMethod) {
      final Location location = settings.getLocation();
      if (location == null) {
        audience.sendMessage(Locale.ERR_LOC.build());
        return SINGLE_SUCCESS;
      }
    }
    method.setup();

    this.createBrowser(settings, method, url);

    audience.sendMessage(Locale.INFO_BROWSER_LOAD.build(url));

    return SINGLE_SUCCESS;
  }

  private void createBrowser(
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url) {
    this.neon.shutdownBrowser();
    final SeleniumBrowser browser = new SeleniumBrowser(settings, method);
    this.neon.setBrowser(browser);
    browser.setURL(url);
    browser.start();
  }

  private boolean checkUrl(@NotNull final String url) {
    try {
      new URL(url).openConnection().connect();
      return true;
    } catch (final IOException e) {
      return false;
    }
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
