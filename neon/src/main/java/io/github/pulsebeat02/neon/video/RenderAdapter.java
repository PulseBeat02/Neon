package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import org.jetbrains.annotations.NotNull;

public abstract class RenderAdapter implements RenderMethod {

  private @NotNull final Neon neon;

  public RenderAdapter(@NotNull final Neon neon) {
    this.neon = neon;
  }

  @Override
  public void setup() {}

  @Override
  public void destroy() {}

  public @NotNull Neon getNeon() {
    return this.neon;
  }
}
