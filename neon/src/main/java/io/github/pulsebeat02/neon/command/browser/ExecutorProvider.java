package io.github.pulsebeat02.neon.command.browser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;

public final class ExecutorProvider {

  public static @NotNull final ExecutorService BROWSER_SERVICE;

  static {
    BROWSER_SERVICE = Executors.newCachedThreadPool();
  }

  public static void init() {}

  public static void shutdown() {
    BROWSER_SERVICE.shutdownNow();
  }
}
