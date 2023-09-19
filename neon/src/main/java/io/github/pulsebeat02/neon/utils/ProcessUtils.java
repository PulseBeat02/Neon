package io.github.pulsebeat02.neon.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class ProcessUtils {

  private ProcessUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static void captureOutput(
      @NotNull final ProcessBuilder builder, @NotNull final Consumer<String> console)
      throws IOException {
    final Process p = builder.start();
    String line;
    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    BufferedReader rdr = new BufferedReader(isr);
    while ((line = rdr.readLine()) != null) {
      console.accept(line);
    }
    isr = new InputStreamReader(p.getErrorStream());
    rdr = new BufferedReader(isr);
    while ((line = rdr.readLine()) != null) {
      console.accept(line);
    }
    try {
      p.waitFor();
      isr.close();
      rdr.close();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void setEnvironmentalVariable(@NotNull final String key, @NotNull final String value) {
    try {
      final Map<String, String> unwritable = System.getenv();
      final Map<String, String> writable =
          (Map<String, String>) UnsafeUtils.getField(unwritable, "m");
      writable.put(key, value);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }
}
