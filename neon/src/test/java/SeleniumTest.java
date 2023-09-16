import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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

    int[] raster = get2DPixelArrayFast(raw);

    BufferedImage image = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, raw.getWidth(), raw.getHeight(), raster, 0, raw.getWidth());

    final String path = System.getProperty("user.dir") + "/test.png";
    ImageIO.write(image, "png", new File(path));

    driver.quit();
  }

  public static int[] get2DPixelArrayFast(@NotNull final BufferedImage image) {
    byte[] pixelData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    int width = image.getWidth();
    int height = image.getHeight();
    boolean hasAlphaChannel = image.getAlphaRaster() != null;
    int[] result = new int[height * width];
    if (hasAlphaChannel) {
      int numberOfValues = 4;
      for (int valueIndex = 0, row = 0, col = 0; valueIndex + numberOfValues - 1 < pixelData.length; valueIndex += numberOfValues) {
        int argb = 0;
        argb += (((int) pixelData[valueIndex] & 0xff) << 24); // alpha value
        argb += ((int) pixelData[valueIndex + 1] & 0xff); // blue value
        argb += (((int) pixelData[valueIndex + 2] & 0xff) << 8); // green value
        argb += (((int) pixelData[valueIndex + 3] & 0xff) << 16); // red value
        result[row * col] = argb;
        col++;
        if (col == width) {
          col = 0;
          row++;
        }
      }
    } else {
      int numberOfValues = 3;
      for (int valueIndex = 0, row = 0, col = 0; valueIndex + numberOfValues - 1 < pixelData.length; valueIndex += numberOfValues) {
        int argb = 0;
        argb += -16777216; // 255 alpha value (fully opaque)
        argb += ((int) pixelData[valueIndex] & 0xff); // blue value
        argb += (((int) pixelData[valueIndex + 1] & 0xff) << 8); // green value
        argb += (((int) pixelData[valueIndex + 2] & 0xff) << 16); // red value
        result[row * col] = argb;
        col++;
        if (col == width) {
          col = 0;
          row++;
        }
      }
    }
    return result;
  }
}
