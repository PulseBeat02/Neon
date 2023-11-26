package io.github.pulsebeat02.neon.listener;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.github.pulsebeat02.neon.packet.NeonFrameUpdateS2CPacket;
import io.github.pulsebeat02.neon.packet.NeonMapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class MapDataMessagingHandler {

  private final SimpleChannel data;

  public MapDataMessagingHandler() {
    this.data =
        ChannelBuilder.named("neon:map_data").optional().networkProtocolVersion(1).simpleChannel();
    this.data.messageBuilder(NeonFrameUpdateS2CPacket.class).decoder(this::recieve);
  }

  private @NotNull NeonFrameUpdateS2CPacket recieve(@NotNull final FriendlyByteBuf buf) {
    final NeonFrameUpdateS2CPacket packet =
        NeonFrameUpdateS2CPacket.deserialize(buf.readByteArray());
    for (final NeonMapPacket mapPacket : packet.frames) {
      final int id = mapPacket.id;
      final int centerX = mapPacket.centerX;
      final int centerZ = mapPacket.centerZ;
      final byte[] mapData = mapPacket.mapData;
      this.handleMapData(id, centerX, centerZ, mapData);
    }
    return packet;
  }

  private void handleMapData(
      final int id, final int centerX, final int centerZ, final byte[] mapData) {
    final Minecraft minecraft = Minecraft.getInstance();
    final Player player = minecraft.player;
    if (player == null) {
      return;
    }
    try (final Level world = player.level()) {
      final String mapId = "map_" + id;
      final ResourceKey<Level> dimension = world.dimension();
      final MapItemSavedData map =
          MapItemSavedData.createFresh(centerX, centerZ, (byte) 0, false, false, dimension);
      map.colors = mapData;
      world.setMapData(mapId, map);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }
}
