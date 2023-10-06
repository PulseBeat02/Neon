package io.github.pulsebeat02.neon.json;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

public final class GsonProvider {

  private static @NotNull final Gson GSON;

  static {
    GSON = new Gson();
  }

  public static @NotNull Gson getGson() {
    return GSON;
  }
}
