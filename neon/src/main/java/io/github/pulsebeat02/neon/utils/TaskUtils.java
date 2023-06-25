package io.github.pulsebeat02.neon.utils;

import io.github.pulsebeat02.neon.Neon;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class TaskUtils {

  private TaskUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static <T> T sync(@NotNull final Neon neon, @NotNull final Callable<T> task) {
    try {
      return Bukkit.getServer().getScheduler().callSyncMethod(neon, task).get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new AssertionError(e);
    }
  }
}
