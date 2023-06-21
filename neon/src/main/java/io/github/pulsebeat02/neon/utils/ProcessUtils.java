package io.github.pulsebeat02.neon.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public final class ProcessUtils {

  private ProcessUtils() {}

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

  
}
