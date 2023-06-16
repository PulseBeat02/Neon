package io.github.pulsebeat02.neon.command.browser;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.Permission.has;

public final class BrowserHomeUrlCommand implements CommandSegment.Literal<CommandSender> {

  private @NotNull final Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;
  private @NotNull final BrowserConfiguration config;

  public BrowserHomeUrlCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("homepage-url")
            .requires(has("neon.command.browser.homepage-url"))
            .then(
                this.argument("url", StringArgumentType.greedyString())
                    .executes(this::setHomepageUrl))
            .build();
  }

  private int setHomepageUrl(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.neon.audience().sender(context.getSource());
    final String url = context.getArgument("url", String.class);
    if (!this.checkHomePageUrl(url)) {
      audience.sendMessage(Locale.INVALID_HOMEPAGE_URL.build());
      return SINGLE_SUCCESS;
    }
    this.config.setHomePageUrl(url);
    audience.sendMessage(Locale.SET_HOMEPAGE_URL.build(url));
    return SINGLE_SUCCESS;
  }

  private boolean checkHomePageUrl(@NotNull final String query) {
    try {
      final URL url = new URL(query);
      url.openStream().close();
      return true;
    } catch (final Exception ex) {
      return false;
    }
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
