package io.github.pulsebeat02.neon.nms;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PacketSender {

  void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final ByteBuf rgb,
      final int map,
      final int mapHeight,
      final int mapWidth,
      final int videoWidth,
      final int xOffset,
      final int yOffset);

  default void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final ByteBuf rgb,
      final int map,
      final int mapWidth,
      final int mapHeight,
      final int videoWidth) {
    final int vidHeight = rgb.capacity() / videoWidth;
    final int pixW = mapWidth << 7;
    final int pixH = mapHeight << 7;
    final int xOff = (pixW - videoWidth) >> 1;
    final int yOff = (pixH - vidHeight) >> 1;
    this.displayMaps(viewers, rgb, map, mapHeight, mapWidth, videoWidth, xOff, yOff);
  }

  void injectPlayer(@NotNull final Player player);

  void uninjectPlayer(@NotNull final Player player);

  boolean isMapRegistered(final int id);

  void unregisterMap(final int id);

  void registerMap(final int id);
}
