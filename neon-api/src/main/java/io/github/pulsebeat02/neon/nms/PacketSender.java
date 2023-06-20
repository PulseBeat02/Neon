package io.github.pulsebeat02.neon.nms;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface PacketSender {

  void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final ByteBuf rgb,
      final int map,
      final int mapWidth,
      final int mapHeight,
      final int videoWidth,
      final int videoHeight);

  void displayEntities(
      @NotNull final UUID[] viewers,
      @NotNull final Location location,
      @NotNull final Entity[] entities,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int height);

  void injectPlayer(@NotNull final UUID player);

  void uninjectPlayer(@NotNull final UUID player);
}
