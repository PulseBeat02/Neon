package io.github.pulsebeat02.neon.command.browser;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.neon.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.command.CommandSegment;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.video.RenderMethod;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.cef.browser.MinecraftBrowser;
import org.jetbrains.annotations.NotNull;

public final class BrowserDestroyCommand implements CommandSegment.Literal<CommandSender> {

  private final @NotNull Neon neon;
  private @NotNull final LiteralCommandNode<CommandSender> node;
  private @NotNull final BrowserConfiguration config;

  public BrowserDestroyCommand(@NotNull final Neon neon) {
    this.neon = neon;
    this.config = neon.getConfiguration();
    this.node =
        this.literal("destroy")
            .requires(has("neon.command.browser.destroy"))
            .executes(this::startBrowser)
            .build();
  }

  private int startBrowser(@NotNull final CommandContext<CommandSender> context) {

    final MinecraftBrowser browser = this.neon.getBrowser();
    this.neon.shutdownBrowser();

    final RenderMethod method = browser.getRenderMethod();
    method.destroy();

    final Audience audience = this.neon.audience().sender(context.getSource());
    audience.sendMessage(Locale.DESTROY_BROWSER.build());

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
