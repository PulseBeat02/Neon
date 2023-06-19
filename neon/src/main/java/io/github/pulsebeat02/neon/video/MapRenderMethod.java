package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public final class MapRenderMethod extends RenderAdapter {

  private final PacketSender sender;
  private final int width;
  private final int height;
  private final int blockWidth;
  private final int blockHeight;

  public MapRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon);
    this.sender = neon.getPacketSender();

    final ImmutableDimension blockDimension = settings.getBlockDimension();
    this.blockWidth = blockDimension.getWidth();
    this.blockHeight = blockDimension.getHeight();

    final ImmutableDimension dimension = settings.getResolution();
    this.width = dimension.getWidth();
    this.height = dimension.getHeight();
  }

  @Override
  public void render(@NotNull final ByteBuf buf) {
    this.sender.displayMaps(
        null, buf, 0, this.blockWidth, this.blockHeight, this.width, this.height);
  }
}
