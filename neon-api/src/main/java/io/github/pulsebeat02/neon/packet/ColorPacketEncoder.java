package io.github.pulsebeat02.neon.packet;

import io.github.pulsebeat02.neon.dither.MapPalette;
import java.awt.*;
import org.jetbrains.annotations.NotNull;

public final class ColorPacketEncoder {

  private static final byte[] INDEXED_COLORS;
  private static final byte[] RGB_TO_INDEX;

  static {
    final Color[] colors = MapPalette.NMS_PALETTE;
    INDEXED_COLORS = new byte[colors.length];
    RGB_TO_INDEX = new byte[128 * 128 * 128];
    for (byte i = 0; i < colors.length; i++) {
      final byte rgb = (byte) colors[i].getRGB();
      INDEXED_COLORS[i] = rgb;
      RGB_TO_INDEX[rgb] = i;
    }
  }

  private ColorPacketEncoder() {
    throw new UnsupportedOperationException();
  }

  public static byte @NotNull [] encode(final byte @NotNull [] colors) {
    final byte[] result = new byte[colors.length];
    for (int i = 0; i < colors.length; i++) {
      result[i] = RGB_TO_INDEX[colors[i]];
    }
    return result;
  }

  public static byte @NotNull [] decode(final byte @NotNull [] indexes) {
    final byte[] result = new byte[indexes.length];
    for (int i = 0; i < indexes.length; i++) {
      result[i] = INDEXED_COLORS[indexes[i]];
    }
    return result;
  }
}
