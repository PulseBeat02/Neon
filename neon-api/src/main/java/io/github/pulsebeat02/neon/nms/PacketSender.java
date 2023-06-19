package io.github.pulsebeat02.neon.nms;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PacketSender {

  void displayMaps(
      @Nullable final UUID[] viewers,
      @NotNull final ByteBuf rgb,
      final int map,
      final int mapHeight,
      final int mapWidth,
      final int videoWidth,
      final int xOffset,
      final int yOffset);

  default void displayMaps(
      @Nullable final UUID[] viewers,
      @NotNull final ByteBuf rgb,
      final int map,
      final int mapWidth,
      final int mapHeight,
      final int width,
      final int height) {
    final int pixW = mapWidth << 7;
    final int pixH = mapHeight << 7;
    final int xOff = (pixW - width) >> 1;
    final int yOff = (pixH - height) >> 1;
    this.displayMaps(viewers, rgb, map, mapHeight, mapWidth, width, xOff, yOff);
  }

  void injectPlayer(@NotNull final UUID player);

  void uninjectPlayer(@NotNull final UUID player);
}
