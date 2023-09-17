package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.SeleniumBrowser;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.netty.buffer.ByteBuf;
import java.nio.IntBuffer;
import org.jetbrains.annotations.NotNull;

public final class MapRenderMethod extends RenderAdapter {

  private @NotNull final Neon neon;
  private @NotNull final PacketSender sender;
  private final int width;
  private final int height;
  private final int blockWidth;
  private final int blockHeight;

  public MapRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon);
    this.sender = neon.getPacketSender();
    this.neon = neon;
    final ImmutableDimension blockDimension = settings.getBlockDimension();
    this.blockWidth = blockDimension.getWidth();
    this.blockHeight = blockDimension.getHeight();
    final ImmutableDimension dimension = settings.getResolution();
    this.width = dimension.getWidth();
    this.height = dimension.getHeight();
  }

  @Override
  public void render(@NotNull final IntBuffer buf) {
    final SeleniumBrowser browser = this.neon.getBrowser();
    if (browser != null) {
      final ByteBuf buffer = browser.getSettings().getHandler().dither(buf, this.width);
      this.sender.displayMaps(
          null, buffer, 0, this.blockWidth, this.blockHeight, this.width, this.height);
    }
  }
}
