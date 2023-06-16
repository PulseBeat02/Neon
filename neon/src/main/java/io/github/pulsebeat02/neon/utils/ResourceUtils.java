package io.github.pulsebeat02.neon.utils;

import java.io.InputStream;

public final class ResourceUtils {

  private ResourceUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static InputStream getResourceAsStream(final String name) {
    return ResourceUtils.class.getClassLoader().getResourceAsStream(name);
  }
}
