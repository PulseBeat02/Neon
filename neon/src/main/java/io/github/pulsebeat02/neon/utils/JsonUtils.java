package io.github.pulsebeat02.neon.utils;

import com.google.gson.Gson;
import io.github.pulsebeat02.neon.json.GsonProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public final class JsonUtils {

  private static @NotNull final Gson GSON;

  static {
    GSON = GsonProvider.getGson();
  }

  private JsonUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static <T> List<T> toListFromResource(@NotNull final String resource) throws IOException {
    try (final Reader reader = ResourceUtils.getResourceAsReader(resource)) {
      return GSON.fromJson(reader, List.class);
    }
  }

  public static <T, V> Map<T, V> toMapFromResource(@NotNull final String resource)
      throws IOException {
    try (final Reader reader = ResourceUtils.getResourceAsReader(resource)) {
      return GSON.fromJson(reader, Map.class);
    }
  }
}
