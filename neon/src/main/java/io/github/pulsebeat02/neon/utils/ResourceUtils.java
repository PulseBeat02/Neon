package io.github.pulsebeat02.neon.utils;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.jetbrains.annotations.NotNull;

public final class ResourceUtils {

  private ResourceUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static @NotNull InputStream getResourceAsStream(@NotNull final String name) {
    return requireNonNull(ResourceUtils.class.getClassLoader().getResourceAsStream(name));
  }

  public static @NotNull Reader getResourceAsReader(@NotNull final String name) {
    return new BufferedReader(new InputStreamReader(getResourceAsStream(name)));
  }
}
