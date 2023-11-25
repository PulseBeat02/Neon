package io.github.pulsebeat02.neon.messaging;

import io.github.pulsebeat02.neon.Neon;
import org.bukkit.Server;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;

public final class MessagingHandler {

  private static final String CHANNEL_ID;

  static {
    CHANNEL_ID = "neon:map_data";
  }

  private @NotNull final Neon neon;

  public MessagingHandler(@NotNull final Neon neon) {
    this.neon = neon;
  }

  public void registerMessagingHandler() {
    final Server server = this.neon.getServer();
    final Messenger messenger = server.getMessenger();
    messenger.registerIncomingPluginChannel(
        this.neon,
        CHANNEL_ID,
        (channel, player, message) ->
            this.neon.getPacketSender().sendModPacket(player.getUniqueId()));
  }

  public void unregisterMessagingHandler() {
    final Server server = this.neon.getServer();
    final Messenger messenger = server.getMessenger();
    messenger.unregisterIncomingPluginChannel(this.neon, CHANNEL_ID);
  }
}
