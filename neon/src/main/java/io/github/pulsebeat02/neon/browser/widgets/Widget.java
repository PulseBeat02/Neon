package io.github.pulsebeat02.neon.browser.widgets;

import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;

public enum Widget {
  BACK(createWidget("browser/widgets/back.png", 0, 0)),
  FORWARD(createWidget("browser/widgets/forward.png", 20, 0)),
  REFRESH(createWidget("browser/widgets/refresh.png", 40, 0)),
  HOME(createWidget("browser/widgets/home.png", 60, 0));

  private @NotNull final BrowserWidget widget;

  Widget(@NotNull final BrowserWidget widget) {
    this.widget = widget;
  }

  public @NotNull BrowserWidget getWidget() {
    return this.widget;
  }

  public static @NotNull BrowserWidget createWidget(
      @NotNull final String resource, final int x, final int y) {
    try (final InputStream stream = ResourceUtils.getResourceAsStream(resource)) {
      final BufferedImage image = ImageIO.read(stream);
      final int w = image.getWidth();
      final int h = image.getHeight();
      final int[] buffer = image.getRGB(0, 0, w, h, null, 0, w);
      return new BrowserWidget(IntBuffer.wrap(buffer), x, y);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
