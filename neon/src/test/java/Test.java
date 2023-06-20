import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.cef.browser.TestBrowser;

public class Test {

  public static void main(final String[] args){

    final JLabel label = new JLabel();
    label.setVisible(true);

    final TestBrowser browser = new TestBrowser("https://www.google.com/", (buffer) -> {
      try {
        Thread.sleep(10L);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
      final BufferedImage image = new BufferedImage(640, 640, BufferedImage.TYPE_INT_BGR);
      label.setIcon(new ImageIcon(image));
    });

    final JFrame frame = new JFrame("Test");
    frame.setSize(640, 640);
    frame.add(label);
    frame.setVisible(true);

    browser.loadURL("https://www.google.com/");

  }

}
