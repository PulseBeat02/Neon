package io.github.pulsebeat02.neon.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

import java.io.ByteArrayOutputStream;

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
    final int id = buf.readInt();
    final double centerX = buf.readDouble();
    final double centerZ = buf.readDouble();
    final byte[] squishedMapData = buf.readByteArray();
    final byte[] mapData = this.expand(squishedMapData);
    client.execute(() -> this.handleMapData(client, id, centerX, centerZ, mapData));
  }

  private byte @NotNull [] expand(final byte @NotNull [] squishedMapData) {
    try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      for (int i = 0; i < squishedMapData.length; i += 2) {
        final int count = squishedMapData[i];
        final byte value = squishedMapData[i + 1];
        for (int j = 0; j < count; j++) {
          out.write(value);
        }
      }
      return out.toByteArray();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void handleMapData(
      @NotNull final MinecraftClient client,
      final int id,
      final double centerX,
      final double centerZ,
      final byte[] mapData) {
    final ClientPlayerEntity player = client.player;
    if (player == null) {
      return;
    }
    final ClientWorld world = player.clientWorld;
    final RegistryKey<World> key = world.getRegistryKey();
    final MapState state = MapState.of(centerX, centerZ, (byte) 0, false, false, key);
    state.colors = mapData;
    world.putClientsideMapState(String.valueOf(id), state);
  }
}
