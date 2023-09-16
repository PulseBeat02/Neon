package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public final class SeleniumBrowser {

  private @NotNull final ExecutorService executor;
  private @NotNull final WebDriver driver;
  private @NotNull final Neon neon;
  private @NotNull final BrowserSettings settings;
  private @NotNull final RenderMethod method;
  private @NotNull final String url;
  private @NotNull final AtomicBoolean running;

  public SeleniumBrowser(
      @NotNull final Neon neon,
      @NotNull final BrowserSettings settings,
      @NotNull final RenderMethod method,
      @NotNull final String url) {
    this.driver = new ChromeDriver(this.createArguments());
    this.executor = Executors.newSingleThreadExecutor();
    this.neon = neon;
    this.settings = settings;
    this.method = method;
    this.url = url;
    this.running = new AtomicBoolean(true);
    this.setupDriver();
  }

  public void start() {
    this.startPaintLoop();
  }

  public void shutdown() {
    this.running.set(false);
    this.driver.quit();
    this.executor.shutdownNow();
  }

  public void sendMouseEvent(final int x, final int y, @NotNull final MouseClick type) {
    final Actions actions = this.getActions();
    final Actions move = actions.moveByOffset(x, y);
    final Actions modified =
        switch (type) {
          case LEFT -> move.click();
          case RIGHT -> move.contextClick();
          case DOUBLE -> move.doubleClick();
          case HOLD -> move.clickAndHold();
          case RELEASE -> move.release();
        };
    modified.build().perform();
  }

  public void loadURL(@NotNull final String url) {
    this.driver.get(url);
  }

  private void startPaintLoop() {
    CompletableFuture.runAsync(this::paintLoop, this.executor);
  }

  private void paintLoop() {
    final long previous = System.currentTimeMillis();
    while (this.running.get()) {
      try {
        final long current = System.currentTimeMillis();
        final long elapsed = current - previous;
        if (elapsed < 42) {
          continue;
        }
        this.paint();
      } catch (final IOException e) {
        this.neon.logConsole(e.getMessage());
      }
    }
  }

  private void setupDriver() {
    this.setDimensions();
  }

  private void setDimensions() {
    final WebDriver.Options options = this.driver.manage();
    final WebDriver.Window window = options.window();
    final ImmutableDimension immutable = this.settings.getResolution();
    final int width = immutable.getWidth();
    final int height = immutable.getHeight();
    final Dimension dimension = new Dimension(width, height);
    window.setSize(dimension);
  }

  private @NotNull ChromeOptions createArguments() {
    final ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    return options;
  }

  private void paint() throws IOException {
    final TakesScreenshot screenshot = (TakesScreenshot) this.driver;
    final byte[] bytes = screenshot.getScreenshotAs(OutputType.BYTES);
    final BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
    final int width = image.getWidth();
    final int height = image.getHeight();
    final int[] raster = image.getRGB(0, 0, width, height, null, 0, width);
    this.method.render(IntBuffer.wrap(raster));
  }

  private @NotNull Actions getActions() {
    final Actions actions = new Actions(this.driver);
    final WebElement fake = this.driver.findElement(By.tagName("body"));
    actions.moveToElement(fake, 0, 0);
    return actions;
  }

  public @NotNull ExecutorService getExecutor() {
    return this.executor;
  }

  public @NotNull WebDriver getDriver() {
    return this.driver;
  }

  public @NotNull Neon getNeon() {
    return this.neon;
  }

  public @NotNull BrowserSettings getSettings() {
    return this.settings;
  }

  public @NotNull RenderMethod getRenderMethod() {
    return this.method;
  }

  public @NotNull String getUrl() {
    return this.url;
  }

  public @NotNull AtomicBoolean getRunning() {
    return this.running;
  }
}
