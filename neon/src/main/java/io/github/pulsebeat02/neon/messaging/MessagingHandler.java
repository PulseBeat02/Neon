package io.github.pulsebeat02.neon.messaging;

import io.github.pulsebeat02.neon.Neon;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;

public final class MessagingHandler {

  private static final String CHANNEL_ID;

  static {
    CHANNEL_ID = "neon:info";
  }

  private @NotNull final Neon neon;

  public MessagingHandler(@NotNull final Neon neon) {
    this.neon = neon;
  }

  public void registerMessagingHandler() {
    final Server server = this.neon.getServer();
    final Messenger messenger = server.getMessenger();
    messenger.registerIncomingPluginChannel(this.neon, CHANNEL_ID, this::register);
  }

  public void register(
      @NotNull final String id, @NotNull final Player player, final byte @NotNull [] bytes) {
    final Server server = this.neon.getServer();
    final Messenger messenger = server.getMessenger();
    messenger.unregisterIncomingPluginChannel(this.neon, CHANNEL_ID);
  }

  public void unregisterMessagingHandler() {
    final Server server = this.neon.getServer();
    final Messenger messenger = server.getMessenger();
    messenger.unregisterIncomingPluginChannel(this.neon, CHANNEL_ID);
  }
}
