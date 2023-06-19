package io.github.pulsebeat02.neon.dither;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jetbrains.annotations.NotNull;

public interface DitherHandler {

  @NotNull
  ByteBuf dither(@NotNull final ByteBuffer raster, final int blockWidth);
}
