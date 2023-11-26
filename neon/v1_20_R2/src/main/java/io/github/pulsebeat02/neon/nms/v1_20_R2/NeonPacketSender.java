/*
 * MIT License
 *
 * Copyright (c) 2024 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.neon.nms.v1_20_R2;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.packet.NeonFrameUpdateS2CPacket;
import io.github.pulsebeat02.neon.packet.NeonMapPacket;
import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NeonPacketSender implements PacketSender {
  private static final int PACKET_THRESHOLD_MS;
  private static final Set<Object> PACKET_DIFFERENTIATION;
  private static final String HANDLER_NAME;
  private static final String MESSENGER_ID;

  static {
    PACKET_THRESHOLD_MS = 0;
    PACKET_DIFFERENTIATION = Collections.newSetFromMap(new WeakHashMap<>());
    HANDLER_NAME = "neon_handler_1171";
    MESSENGER_ID = "neon:map_data";
  }

  private final Map<UUID, PlayerConnection> connections;
  private final Map<UUID, Long> lastUpdated;
  private final Set<UUID> modUsage;
  private final Plugin plugin;

  public NeonPacketSender() {
    this.connections = new ConcurrentHashMap<>();
    this.lastUpdated = new ConcurrentHashMap<>();
    this.modUsage = new HashSet<>();
    this.plugin = Bukkit.getPluginManager().getPlugin("Neon");
    this.registerMessenger();
  }

  private void registerMessenger() {
    final Server server = this.plugin.getServer();
    final Messenger messenger = server.getMessenger();
    messenger.registerOutgoingPluginChannel(this.plugin, MESSENGER_ID);
  }

  @Override
  public void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final ByteBuf rgb,
      final int map,
      final int mapWidth,
      final int mapHeight,
      final int videoWidth,
      final int videoHeight) {
    final int pixW = mapWidth << 7;
    final int pixH = mapHeight << 7;
    final int xOff = (pixW - videoWidth) >> 1;
    final int yOff = (pixH - videoHeight) >> 1;
    final int negXOff = xOff + videoWidth;
    final int negYOff = yOff + videoHeight;
    final int xLoopMin = Math.max(0, xOff >> 7);
    final int yLoopMin = Math.max(0, yOff >> 7);
    final int xLoopMax = Math.min(mapWidth, (int) Math.ceil(negXOff / 128.0));
    final int yLoopMax = Math.min(mapHeight, (int) Math.ceil(negYOff / 128.0));
    final int size = (xLoopMax - xLoopMin) * (yLoopMax - yLoopMin);
    final PacketPlayOutMap[] packetArray = new PacketPlayOutMap[size];
    final NeonMapPacket[] neonPackets = new NeonMapPacket[size];
    int arrIndex = 0;
    for (int y = yLoopMin; y < yLoopMax; y++) {
      final int relY = y << 7;
      final int topY = Math.max(0, yOff - relY);
      final int yDiff = Math.min(128 - topY, negYOff - (relY + topY));
      for (int x = xLoopMin; x < xLoopMax; x++) {
        final int relX = x << 7;
        final int topX = Math.max(0, xOff - relX);
        final int xDiff = Math.min(128 - topX, negXOff - (relX + topX));
        final int xPixMax = xDiff + topX;
        final int yPixMax = yDiff + topY;
        final byte[] mapData = new byte[xDiff * yDiff];
        for (int iy = topY; iy < yPixMax; iy++) {
          final int yPos = relY + iy;
          final int indexY = (yPos - yOff) * videoWidth;
          for (int ix = topX; ix < xPixMax; ix++) {
            mapData[(iy - topY) * xDiff + ix - topX] = rgb.getByte(indexY + relX + ix - xOff);
          }
        }
        final int mapId = map + mapWidth * y + x;
        final List<MapIcon> icons = new ArrayList<>();
        final WorldMap.b worldmap = new WorldMap.b(topX, topY, xDiff, yDiff, mapData);
        final PacketPlayOutMap packet =
            new PacketPlayOutMap(mapId, (byte) 0, false, icons, worldmap);
        packetArray[arrIndex] = packet;
        neonPackets[arrIndex] = new NeonMapPacket(mapId, topX, topY, mapData); // TODO this
        arrIndex++;
        PACKET_DIFFERENTIATION.add(packet);
      }
    }
    this.sendMapPackets(viewers, packetArray, neonPackets);
  }

  private void sendMapPackets(
      final UUID[] viewers,
      final PacketPlayOutMap[] packetArray,
      final NeonMapPacket[] neonPackets) {
    if (viewers == null) {
      for (final UUID uuid : this.connections.keySet()) {
        this.sendMapPacketsToViewers(uuid, packetArray, neonPackets);
      }
    } else {
      for (final UUID uuid : viewers) {
        this.sendMapPacketsToViewers(uuid, packetArray, neonPackets);
      }
    }
  }

  private void sendMapPacketsToViewers(
      final UUID uuid, final PacketPlayOutMap[] packetArray, final NeonMapPacket[] neonPackets) {
    final long val = this.lastUpdated.getOrDefault(uuid, 0L);
    if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
      final PlayerConnection connection = this.connections.get(uuid);
      if (connection == null) {
        return;
      }
      this.updateTime(uuid);
      if (this.modUsage.contains(uuid)) {
        final Player player = Bukkit.getPlayer(uuid);
        final NeonFrameUpdateS2CPacket packet = new NeonFrameUpdateS2CPacket(neonPackets);
        player.sendPluginMessage(this.plugin, MESSENGER_ID, packet.serialize());
      } else {
        for (final PacketPlayOutMap packet : packetArray) {
          connection.a(packet);
        }
      }
    }
  }

  private void updateTime(final UUID uuid) {
    this.lastUpdated.put(uuid, System.currentTimeMillis());
  }

  @Override
  public void injectPlayer(@NotNull final UUID player) {
    final Player bukkitPlayer = requireNonNull(Bukkit.getPlayer(player));
    final PlayerConnection conn = ((CraftPlayer) bukkitPlayer).getHandle().c;
    final NetworkManager manager =
        (NetworkManager)
            UnsafeUtils.getFieldExceptionally(ServerCommonPacketListenerImpl.class, conn, "c");
    final Channel channel = manager.n;
    this.addChannelPipeline(channel);
    this.addConnection(bukkitPlayer, conn);
  }

  private void addChannelPipeline(@NotNull final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void addConnection(@NotNull final Player player, @NotNull final PlayerConnection conn) {
    this.connections.put(player.getUniqueId(), conn);
  }

  @Override
  public void uninjectPlayer(@NotNull final UUID player) {
    final Player bukkitPlayer = requireNonNull(Bukkit.getPlayer(player));
    final PlayerConnection conn = ((CraftPlayer) bukkitPlayer).getHandle().c;
    final NetworkManager manager =
        (NetworkManager)
            UnsafeUtils.getFieldExceptionally(ServerCommonPacketListenerImpl.class, conn, "c");
    final Channel channel = manager.n;
    this.removeChannelPipeline(channel);
    this.removeConnection(bukkitPlayer);
    this.removeModInfo(player);
  }

  private void removeModInfo(@NotNull final UUID uuid) {
    this.modUsage.remove(uuid);
  }

  @Override
  public void sendModPacket(@NotNull final UUID uuid) {
    this.modUsage.add(uuid);
  }

  private void removeChannelPipeline(@Nullable final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void removeChannelPipelineHandler(@NotNull final Channel channel) {
    final ChannelPipeline pipeline = channel.pipeline();
    if (pipeline.get(HANDLER_NAME) != null) {
      pipeline.remove(HANDLER_NAME);
    }
  }

  private void removeConnection(@NotNull final Player player) {
    this.connections.remove(player.getUniqueId());
  }
}
