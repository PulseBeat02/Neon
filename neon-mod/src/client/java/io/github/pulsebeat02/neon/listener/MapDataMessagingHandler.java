package io.github.pulsebeat02.neon.listener;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.map.MapState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class MapDataMessagingHandler implements ClientPlayNetworking.PlayChannelHandler {

  /*

  INT id
  DOUBLE centerX
  DOUBLE centerZ
  BYTE scale

   */
  @Override
  public void receive(
      @NotNull final MinecraftClient client,
      @NotNull final ClientPlayNetworkHandler handler,
      @NotNull final PacketByteBuf buf,
      @NotNull final PacketSender responseSender) {
    final int id = buf.readInt();
    final double centerX = buf.readDouble();
    final double centerZ = buf.readDouble();
    final byte scale = buf.readByte();
    final byte[] mapData = buf.readByteArray();
    client.execute(() -> this.handleMapData(client, id, centerX, centerZ, scale, mapData));
  }

  private void handleMapData(
      @NotNull final MinecraftClient client,
      final int id,
      final double centerX,
      final double centerZ,
      final byte scale,
      final byte[] mapData) {
    final ClientPlayerEntity player = client.player;
    if (player == null) {
      return;
    }
    final ClientWorld world = player.clientWorld;
    final RegistryKey<World> key = world.getRegistryKey();
    final MapState state = MapState.of(centerX, centerZ, scale, false, false, key);
    state.colors = mapData;
    world.putClientsideMapState(String.valueOf(id), state);
  }
}
