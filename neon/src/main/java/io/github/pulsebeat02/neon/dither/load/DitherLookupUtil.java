package io.github.pulsebeat02.neon.dither.load;

import io.github.pulsebeat02.neon.dither.MapPalette;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class DitherLookupUtil {

  public static final int[] PALETTE;
  public static final byte[] COLOR_MAP;
  public static final int[] FULL_COLOR_MAP;

  static {
    COLOR_MAP = new byte[128 * 128 * 128];
    FULL_COLOR_MAP = new int[128 * 128 * 128];
    final List<Integer> colors = getPaletteColors();
    PALETTE = new int[colors.size()];
    updateIndices(colors);
    createLookupTable(forkRed());
  }

  private static void createLookupTable(@NotNull final List<LoadRed> tasks) {
    for (int i = 0; i < 128; i++) {
      final byte[] sub = tasks.get(i).join();
      final int ci = i << 14;
      for (int si = 0; si < 16384; si++) {
        COLOR_MAP[ci + si] = sub[si];
        FULL_COLOR_MAP[ci + si] = PALETTE[Byte.toUnsignedInt(sub[si])];
      }
    }
  }

  private static @NotNull List<LoadRed> forkRed() {
    final List<LoadRed> tasks = new ArrayList<>(128);
    for (int r = 0; r < 256; r += 2) {
      final LoadRed red = new LoadRed(PALETTE, r);
      tasks.add(red);
      red.fork();
    }
    return tasks;
  }

  private static void updateIndices(@NotNull final List<Integer> colors) {
    int index = 0;
    for (final int color : colors) {
      PALETTE[index++] = color;
    }
    PALETTE[0] = 0;
  }

  private static @NotNull List<Integer> getPaletteColors() {
    final List<Integer> colors = new ArrayList<>();
    for (int i = 0; i < 256; ++i) {
      try {
        final Color color = MapPalette.getColor((byte) i);
        colors.add(color.getRGB());
      } catch (final IndexOutOfBoundsException e) {
        break;
      }
    }
    return colors;
  }

  public static int[] getPalette() {
    return PALETTE;
  }

  public static byte[] getColorMap() {
    return COLOR_MAP;
  }

  public static int[] getFullColorMap() {
    return FULL_COLOR_MAP;
  }

  /** Init. */
  public static void init() {}
}
