package io.github.pulsebeat02.neon.dither.load;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import org.jetbrains.annotations.NotNull;

public final class LoadRed extends RecursiveTask<byte[]> {

  @Serial private static final long serialVersionUID = -6408377810782246185L;

  private final int r;
  private final int[] palette;

  LoadRed(final int @NotNull [] palette, final int r) {
    this.r = r;
    this.palette = palette;
  }

  @Override
  protected byte @NotNull [] compute() {
    final List<LoadGreen> greenSub = new ArrayList<>(128);
    this.forkGreen(greenSub);
    return this.copyColors(greenSub);
  }

  private void forkGreen(@NotNull final List<LoadGreen> greenSub) {
    for (int g = 0; g < 256; g += 2) {
      final LoadGreen green = new LoadGreen(this.palette, this.r, g);
      greenSub.add(green);
      green.fork();
    }
  }

  private byte @NotNull [] copyColors(@NotNull final List<LoadGreen> greenSub) {
    final byte[] values = new byte[16384];
    for (int i = 0; i < 128; i++) {
      final byte[] sub = greenSub.get(i).join();
      final int index = i << 7;
      System.arraycopy(sub, 0, values, index, 128);
    }
    return values;
  }
}
