package io.github.pulsebeat02.neon.nms;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface PacketSender {

  void displayMaps(
      final UUID[] viewers,
      final @NotNull ByteBuf rgb,
      final int map,
      final int mapWidth,
      final int mapHeight,
      final int videoWidth);

  void displayEntities(
      @NotNull final UUID[] viewers,
      @NotNull final Location location,
      @NotNull final Entity[] entities,
      @NotNull final ByteBuf data,
      @NotNull final String character,
      final int width,
      final int height);

  void injectPlayer(@NotNull final UUID player);

  void uninjectPlayer(@NotNull final UUID player);
}
