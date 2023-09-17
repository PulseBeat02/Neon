import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumTest {

  public static void main(final String[] args) throws IOException {

    final ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");

    final WebDriver driver = new ChromeDriver(options);
    driver.manage().window().setSize(new Dimension(1024, 1024));
    driver.get("https://selenium.dev");

    final TakesScreenshot screenshot = (TakesScreenshot) driver;
    final byte[] bytes = screenshot.getScreenshotAs(OutputType.BYTES);
    final BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
    
    final int width = raw.getWidth();
    final int height = raw.getHeight();
    int counter = 0;
    final int[] buffer = new int[width * height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        final int argb = raw.getRGB(j, i);
        final int alpha = argb >> 24 & 0xFF;
        final int red = argb >> 16 & 0xFF;
        final int green = argb >> 8 & 0xFF;
        final int blue = argb & 0xFF;
        buffer[counter++] =
                (red * alpha / 255) << 16 | (green * alpha / 255) << 8 | (blue * alpha / 255);
      }
    }

    final BufferedImage img = plotRGB(buffer, width, height);
    ImageIO.write(img, "png", new File("test.png"));
    
    driver.quit();
  }

  public static BufferedImage plotRGB(final int[] rgb, final int width, final int height) {
    final BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      final int base = y * width;
      for (int x = 0; x < width; x++) {
        b.setRGB(x, y, rgb[base + x]);
      }
    }
    return b;
  }

}
