package io.github.pulsebeat02.neon.nms.v1_20_R1;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcher.b;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class NeonPacketSender implements PacketSender {
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
        final int mapId = map + mapWidth * y + x;
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
      for (final UUID uuid : this.connections.keySet()) {
        this.sendMapPacketsToViewers(uuid, packetArray);
      }
    } else {
      for (final UUID uuid : viewers) {
        this.sendMapPacketsToViewers(uuid, packetArray);
      }
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
      for (final PacketPlayOutMap packet : packetArray) {
        connection.a(packet);
      }
    }
  }

  private void updateTime(final UUID uuid) {
    this.lastUpdated.put(uuid, System.currentTimeMillis());
  }

  @Override
  public void displayEntities(
      @NotNull final UUID[] viewers,
      @NotNull final Location location,
      @NotNull final Entity[] entities,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int height) {
    int index = 0;
    for (int i = 0; i < height; i++) {
      final StringBuilder builder = new StringBuilder();
      for (int x = 0; x < width; x++) {
        final int rgb = data.get(index++);
        final ChatColor color = ChatColor.of(String.format("#%06X", rgb & 0xFFFFFF));
        builder.append(color);
        builder.append(character);
      }
      entities[i].setCustomName(builder.toString());
    }
  }

  private void sendEntityPackets(
      @NotNull final UUID[] viewers, @NotNull final PacketPlayOutEntityMetadata[] packets) {
    if (viewers == null) {
      for (final UUID uuid : this.connections.keySet()) {
        this.sendEntityPacketToViewers(uuid, packets);
      }
    } else {
      for (final UUID uuid : viewers) {
        this.sendEntityPacketToViewers(uuid, packets);
      }
    }
  }

  private void sendEntityPacketToViewers(
      @NotNull final UUID uuid, @NotNull final PacketPlayOutEntityMetadata @NotNull [] packets) {
    final PlayerConnection connection = this.connections.get(uuid);
    if (connection == null) {
      return;
    }
    for (final PacketPlayOutEntityMetadata packet : packets) {
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
    final NetworkManager manager = (NetworkManager) UnsafeUtils.getFieldExceptionally(conn, "h");
    final Channel channel = manager.m;
    this.removeChannelPipeline(channel);
    this.removeConnection(bukkitPlayer);
  }

  private void removeChannelPipeline(@NotNull final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void removeChannelPipelineHandler(@NotNull final Channel channel) {
    final ChannelPipeline pipeline = channel.pipeline();
    if (pipeline.get(this.handlerName) != null) {
      pipeline.remove(this.handlerName);
    }
  }

  private void removeConnection(@NotNull final Player player) {
    this.connections.remove(player.getUniqueId());
  }
}
