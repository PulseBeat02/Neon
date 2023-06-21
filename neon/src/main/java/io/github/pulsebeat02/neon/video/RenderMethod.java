package io.github.pulsebeat02.neon.video;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jetbrains.annotations.NotNull;

public interface RenderMethod {

  void setup();

  void render(@NotNull final IntBuffer buf);

  void destroy();
}
