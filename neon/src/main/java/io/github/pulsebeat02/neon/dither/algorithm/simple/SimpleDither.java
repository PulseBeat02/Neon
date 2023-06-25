package io.github.pulsebeat02.neon.dither.algorithm.simple;

import io.github.pulsebeat02.neon.dither.DitherHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.IntBuffer;
import org.bukkit.map.MapPalette;
import org.jetbrains.annotations.NotNull;

public final class SimpleDither implements DitherHandler  {

  @Override
  public @NotNull ByteBuf dither(@NotNull final IntBuffer buffer, final int width) {
    final int length = buffer.capacity();
    final int height = length / width;
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer.get(index);
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = (color) & 0xFF;
        data.writeByte(MapPalette.matchColor(r, g, b));
      }
    }
    return data;
  }
}
