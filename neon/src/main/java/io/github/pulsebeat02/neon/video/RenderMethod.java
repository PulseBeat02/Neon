package io.github.pulsebeat02.neon.video;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface RenderMethod {

  void setup();

  void render(@NotNull final ByteBuf buf);

  void destroy();
}
