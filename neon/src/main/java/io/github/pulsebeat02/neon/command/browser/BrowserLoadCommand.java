package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.MinecraftBrowser;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import java.io.IOException;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
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
            .requires(has("neon.command.browser.load"))
            .executes(this::startBrowser)
            .build();
  }

  private int startBrowser(@NotNull final CommandContext<CommandSender> context) {

    try {
      final BrowserSettings settings = BrowserSettings.ofSettings(this.config);
      final MinecraftBrowser browser = new MinecraftBrowser(this.neon, settings);
      browser.createImmediately();
    } catch (final UnsupportedPlatformException
        | CefInitializationException
        | IOException
        | InterruptedException e) {
      throw new RuntimeException(e);
    }

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
