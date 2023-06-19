package io.github.pulsebeat02.neon.nms.v1_20_R1;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MapPacketSender implements PacketSender {
  private static final int PACKET_THRESHOLD_MS;
  private static final Set<Object> PACKET_DIFFERENTIATION;

  static {
    PACKET_THRESHOLD_MS = 0;
    PACKET_DIFFERENTIATION = Collections.newSetFromMap(new WeakHashMap<>());
  }

  private final Map<UUID, PlayerConnection> connections;
  private final Map<UUID, Long> lastUpdated;
  private final String handlerName;

  {
    this.connections = new ConcurrentHashMap<>();
    this.lastUpdated = new ConcurrentHashMap<>();
    this.handlerName = "neon_handler_1171";
  }

  @Override
  public void displayMaps(
      final UUID[] viewers,
      final @NotNull ByteBuf rgb,
      final int map,
      final int height,
      final int width,
      final int videoWidth,
      final int xOff,
      final int yOff) {
    final int vidHeight = rgb.capacity() / videoWidth;
    final int negXOff = xOff + videoWidth;
    final int negYOff = yOff + vidHeight;
    final int xLoopMin = Math.max(0, xOff >> 7);
    final int yLoopMin = Math.max(0, yOff >> 7);
    final int xLoopMax = Math.min(width, (int) Math.ceil(negXOff / 128.0));
    final int yLoopMax = Math.min(height, (int) Math.ceil(negYOff / 128.0));
    final PacketPlayOutMap[] packetArray =
        new PacketPlayOutMap[(xLoopMax - xLoopMin) * (yLoopMax - yLoopMin)];
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
        final int mapId = map + width * y + x;
        final byte b = (byte) 0;
        final boolean display = false;
        final List<MapIcon> icons = new ArrayList<>();
        final WorldMap.b worldmap = new WorldMap.b(topX, topY, xDiff, yDiff, mapData);
        final PacketPlayOutMap packet = new PacketPlayOutMap(mapId, b, display, icons, worldmap);
        packetArray[arrIndex++] = packet;
        PACKET_DIFFERENTIATION.add(packet);
      }
    }
    this.sendMapPackets(viewers, packetArray);
  }

  private void sendMapPackets(final UUID[] viewers, final PacketPlayOutMap[] packetArray) {
    if (viewers == null) {
      this.sendMapPacketsToAll(packetArray);
    } else {
      this.sendMapPacketsToSpecified(viewers, packetArray);
    }
  }

  private void sendMapPacketsToSpecified(
      final UUID[] viewers, final PacketPlayOutMap[] packetArray) {
    for (final UUID uuid : viewers) {
      this.sendMapPacketsToViewers(uuid, packetArray);
    }
  }

  private void sendMapPacketsToAll(final PacketPlayOutMap[] packetArray) {
    for (final UUID uuid : this.connections.keySet()) {
      this.sendMapPacketsToViewers(uuid, packetArray);
    }
  }

  private void sendMapPacketsToViewers(final UUID uuid, final PacketPlayOutMap[] packetArray) {
    final long val = this.lastUpdated.getOrDefault(uuid, 0L);
    if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
      final PlayerConnection connection = this.connections.get(uuid);
      if (connection == null) {
        return;
      }
      this.updateTime(uuid);
      this.sendSeparatePackets(packetArray, connection);
    }
  }

  private void updateTime(final UUID uuid) {
    this.lastUpdated.put(uuid, System.currentTimeMillis());
  }

  private void sendSeparatePackets(
      final PacketPlayOutMap[] packetArray, final PlayerConnection connection) {
    if (connection == null) {
      return;
    }
    for (final PacketPlayOutMap packet : packetArray) {
      connection.a(packet);
    }
  }

  @Override
  public void injectPlayer(@NotNull final UUID player) {
    final Player bukkitPlayer = requireNonNull(Bukkit.getPlayer(player));
    final PlayerConnection conn = ((CraftPlayer) bukkitPlayer).getHandle().c;
    final NetworkManager manager = (NetworkManager) UnsafeUtils.getFieldExceptionally(conn, "h");
    final Channel channel = manager.m;
    this.addChannelPipeline(channel);
    this.addConnection(bukkitPlayer, conn);
  }

  private void addChannelPipeline(final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void addConnection(final Player player, final PlayerConnection conn) {
    this.connections.put(player.getUniqueId(), conn);
  }

  @Override
  public void uninjectPlayer(@NotNull final UUID player) {
    final Player bukkitPlayer = requireNonNull(Bukkit.getPlayer(player));
    final PlayerConnection conn = ((CraftPlayer) bukkitPlayer).getHandle().c;
    final NetworkManager manager = (NetworkManager) UnsafeUtils.getFieldExceptionally(conn, "h");
    final Channel channel = manager.m;
    this.removeChannelPipeline(channel);
    this.removeConnection(bukkitPlayer);
  }

  private void removeChannelPipeline(final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void removeChannelPipelineHandler(final Channel channel) {
    final ChannelPipeline pipeline = channel.pipeline();
    if (pipeline.get(this.handlerName) != null) {
      pipeline.remove(this.handlerName);
    }
  }
  private void removeConnection(final Player player) {
    this.connections.remove(player.getUniqueId());
  }
}
