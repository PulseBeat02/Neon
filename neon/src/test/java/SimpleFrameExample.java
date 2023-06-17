import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;

public class SimpleFrameExample extends JFrame {
  @Serial private static final long serialVersionUID = -5570653778104813836L;

  private SimpleFrameExample(final String startURL) {
    final CefBrowser browser_ = new ExampleCustomBrowser();
    browser_.loadURL(startURL);
    this.getContentPane().add(browser_.getUIComponent(), BorderLayout.CENTER);
    this.pack();
    this.setSize(800,600);
    this.setVisible(true);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        CefApp.getInstance().dispose();
        SimpleFrameExample.this.dispose();
      }
    });

  }

  public static void main(final String[] args) {
    new SimpleFrameExample("http://www.google.com");
  }
}
