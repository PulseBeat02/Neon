package io.github.pulsebeat02.neon.listener;

import io.github.pulsebeat02.neon.packet.NeonFrameUpdateS2CPacket;
import io.github.pulsebeat02.neon.packet.NeonMapPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;

public final class MapDataMessagingHandler implements ClientPlayNetworking.PlayChannelHandler {

  /*

  INT id
  DOUBLE centerX
  DOUBLE centerZ
  BYTE[] mapData

   */
  @Override
  public void receive(
      @NotNull final MinecraftClient client,
      @NotNull final ClientPlayNetworkHandler handler,
      @NotNull final PacketByteBuf buf,
      @NotNull final PacketSender responseSender) {
    final NeonFrameUpdateS2CPacket packet =
        NeonFrameUpdateS2CPacket.deserialize(buf.readByteArray());
    for (final NeonMapPacket mapPacket : packet.frames) {
      final int id = mapPacket.id;
      final int centerX = mapPacket.centerX;
      final int centerZ = mapPacket.centerZ;
      final byte[] mapData = mapPacket.mapData;
      client.execute(() -> this.handleMapData(client, id, centerX, centerZ, mapData));
    }
  }

  private void handleMapData(
      @NotNull final MinecraftClient client,
      final int id,
      final int centerX,
      final int centerZ,
      final byte[] mapData) {
    final ClientPlayerEntity player = client.player;
    if (player == null) {
      return;
    }
    final ClientWorld world = player.clientWorld;
    final RegistryKey<World> key = world.getRegistryKey();
    final MapState state = MapState.of(centerX, centerZ, (byte) 0, false, false, key);
    state.colors = mapData;
    world.putClientsideMapState(FilledMapItem.getMapName(id), state);
  }
}
