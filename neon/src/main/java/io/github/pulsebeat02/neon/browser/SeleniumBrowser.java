/*
 * MIT License
 *
 * Copyright (c) 2024 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.locale.Locale;
import io.github.pulsebeat02.neon.utils.JsonUtils;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import io.github.pulsebeat02.neon.video.RenderMethod;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput;

public final class SeleniumBrowser {

  private static @NotNull final List<String> CHROME_ARGUMENTS;
  private static @NotNull final String SELENIUM_CACHE;

  static {
    try {
      CHROME_ARGUMENTS = JsonUtils.toListFromResource("browser/settings/config.json");
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    SELENIUM_CACHE = "SE_CACHE_PATH";
    final Neon plugin = (Neon) Bukkit.getPluginManager().getPlugin("Neon");
    setSeleniumCachePath(plugin);
    getSeleniumBinary(plugin);
  }

  private static void setSeleniumCachePath(@NotNull final Neon plugin) {
    final Path parent = plugin.getDataFolder().toPath();
    final Path selenium = parent.resolve("selenium");
    final String path = selenium.toAbsolutePath().toString();
    UnsafeUtils.setEnvironmentalVariable(SELENIUM_CACHE, path);
  }

  private static void getSeleniumBinary(@NotNull final Neon plugin) {
    final BukkitAudiences audience = plugin.audience();
    final Audience console = audience.console();
    console.sendMessage(Locale.INSTALLING_SELENIUM.build());
    final boolean offline = false;
    final ChromeOptions options = new ChromeOptions();
    options.addArguments(CHROME_ARGUMENTS);
    final SeleniumManager manager = SeleniumManager.getInstance();
    final SeleniumManagerOutput.Result result = manager.getDriverPath(options, offline);
    final String path = result.getDriverPath();
    UnsafeUtils.setEnvironmentalVariable("webdriver.chrome.driver", path);
    console.sendMessage(Locale.FINSIHED_INSTALLING_SELENIUM.build());
  }

  public static void init() {}

  private @NotNull final ExecutorService executor;
  private @NotNull final WebDriver driver;
  private @NotNull final BrowserSettings settings;
  private @NotNull final RenderMethod method;
  private @NotNull final AtomicBoolean running;

  public SeleniumBrowser(
      @NotNull final BrowserSettings settings, @NotNull final RenderMethod method) {
    this.driver = new ChromeDriver(this.createArguments());
    this.executor = Executors.newSingleThreadExecutor();
    this.settings = settings;
    this.method = method;
    this.running = new AtomicBoolean(true);
    this.setupDriver();
  }

  public void start() {
    this.startPaintLoop();
  }

  public void shutdown() {
    final RenderMethod method = this.getRenderMethod();
    method.destroy();
    this.running.set(false);
    this.driver.quit();
    this.executor.shutdownNow();
  }

  public void sendMouseEvent(final int x, final int y, @NotNull final MouseClick type) {
    final Actions actions = this.getActions();
    final Actions move = actions.moveByOffset(x, y);
    final Actions modified = this.getAction(type, move);
    modified.build().perform();
  }

  private @NotNull Actions getAction(@NotNull final MouseClick type, @NotNull final Actions move) {
    return switch (type) {
      case LEFT -> move.click();
      case RIGHT -> move.contextClick();
      case DOUBLE -> move.doubleClick();
      case HOLD -> move.clickAndHold();
      case RELEASE -> move.release();
    };
  }

  public void loadURL(@NotNull final String url) {
    this.driver.get(url);
  }

  private void startPaintLoop() {
    CompletableFuture.runAsync(this::paintLoop, this.executor);
  }

  private void paintLoop() {
    final double dt = 1000 / 24D;
    double previous = System.currentTimeMillis();
    while (this.running.get()) {
      final double current = System.currentTimeMillis();
      double frameTime = current - previous;
      previous = current;
      int[] raster = new int[0];
      while (frameTime > 0.0) {
        final double deltaTime = Math.min(frameTime, dt);
        raster = this.getRGB();
        frameTime -= deltaTime;
      }
      this.display(raster);
    }
  }

  private void setupDriver() {
    this.setDimensions();
  }

  private void setDimensions() {
    final ImmutableDimension immutable = this.settings.getResolution();
    final int width = immutable.getWidth();
    final int height = immutable.getHeight();
    this.driver.manage().window().setSize(new Dimension(width, height));
  }

  private @NotNull ChromeOptions createArguments() {
    final ChromeOptions options = new ChromeOptions();
    options.addArguments(CHROME_ARGUMENTS);
    return options;
  }

  private int[] getRGB() {
    final TakesScreenshot screenshot = (TakesScreenshot) this.driver;
    final byte[] bytes = screenshot.getScreenshotAs(OutputType.BYTES);
    final BufferedImage image = this.toBufferedImage(bytes);
    final int width = image.getWidth();
    final int height = image.getHeight();
    int counter = 0;
    final int[] buffer = new int[width * height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        final int argb = image.getRGB(j, i);
        buffer[counter++] = this.convertColorBitmap(argb);
      }
    }
    return buffer;
  }

  private int convertColorBitmap(final int argb) {
    final int alpha = argb >> 24 & 0xFF;
    final int red = argb >> 16 & 0xFF;
    final int green = argb >> 8 & 0xFF;
    final int blue = argb & 0xFF;
    return (red * alpha / 255) << 16 | (green * alpha / 255) << 8 | (blue * alpha / 255);
  }

  private @NotNull BufferedImage toBufferedImage(final byte[] bytes) {
    try (final ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
      return ImageIO.read(stream);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void display(final int[] raster) {
    this.method.render(IntBuffer.wrap(raster));
  }

  private @NotNull Actions getActions() {
    final By tag = By.tagName("body");
    final Actions actions = new Actions(this.driver);
    final WebElement fake = this.driver.findElement(tag);
    actions.moveToElement(fake, 0, 0);
    return actions;
  }

  public @NotNull BrowserSettings getSettings() {
    return this.settings;
  }

  public @NotNull RenderMethod getRenderMethod() {
    return this.method;
  }
}
