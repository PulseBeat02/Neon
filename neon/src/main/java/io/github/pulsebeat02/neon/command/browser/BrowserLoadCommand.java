package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.command.ArgumentUtils;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.video.HologramRenderMethod;
import io.github.pulsebeat02.neon.video.MapRenderMethod;
import io.github.pulsebeat02.neon.video.ParticleRenderMethod;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.io.IOException;
import java.net.URL;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.cef.browser.MinecraftBrowser;
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
            .requires(has("neon.command.browser.load"))
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

    final Component browserError = Locale.INVALID_BROWSER_SETTING.build();
    if (ArgumentUtils.handleNull(audience, browserError, method)) {
      return SINGLE_SUCCESS;
    }

    final Component homepageError = Locale.INVALID_HOMEPAGE_URL.build();
    if (ArgumentUtils.handleFalse(audience, homepageError, this.checkUrl(url))) {
      return SINGLE_SUCCESS;
    }

    final MinecraftBrowser browser = new MinecraftBrowser(this.neon, settings, method, url);
    browser.createImmediately();
    browser.loadURL(url);

    this.neon.setBrowser(browser);

    return SINGLE_SUCCESS;
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
