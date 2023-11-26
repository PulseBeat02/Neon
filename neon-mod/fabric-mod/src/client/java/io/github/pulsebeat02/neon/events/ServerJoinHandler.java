package io.github.pulsebeat02.neon.events;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public final class ServerJoinHandler {

  private static final Identifier SEND_MOD_MESSAGE_ID;

  static {
    SEND_MOD_MESSAGE_ID = new Identifier("neon", "HANDSHAKE");
  }

  public ServerJoinHandler() {
    this.registerEvent();
  }

  private void registerEvent() {
    ClientPlayConnectionEvents.JOIN.register(
        (handler, sender, client) -> this.sendModMessage(client));
  }

  private void sendModMessage(@NotNull final MinecraftClient client) {
    final ClientPlayerEntity player = client.player;
    if (player == null) {
      return;
    }
    final UUID uuid = player.getUuid();
    ClientPlayNetworking.send(
        SEND_MOD_MESSAGE_ID, new PacketByteBuf(Unpooled.buffer()).writeString(uuid.toString()));
  }
}
