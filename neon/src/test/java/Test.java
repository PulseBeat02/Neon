import io.github.pulsebeat02.neon.dither.algorithm.error.FilterLiteDither;
import io.netty.buffer.ByteBuf;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Test {

  public static void main(final String[] args) throws IOException {
    final JFrame frame = new JFrame();
    final FilterLiteDither filter = new FilterLiteDither();
    final BufferedImage original = ImageIO.read(new URL("https://upload.wikimedia.org/wikipedia/commons/7/71/Gradient_color_wheel.png"));
    final int w = original.getWidth();
    final int h = original.getHeight();
    final int[] dataBuffInt = original.getRGB(0, 0, w, h, null, 0, w);
    final ByteBuf dithered = filter.dither(IntBuffer.wrap(dataBuffInt), 626);
    final BufferedImage image = new BufferedImage(626, 626, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        final byte rgb = dithered.readByte();
        image.setRGB(x, y, rgb);
      }
    }
    final JLabel origLabel = new JLabel(new ImageIcon(original));
    final JLabel newLabel = new JLabel(new ImageIcon(image));
    frame.add(origLabel);
    frame.add(newLabel);
    frame.pack();
    frame.setVisible(true);
  }
}
