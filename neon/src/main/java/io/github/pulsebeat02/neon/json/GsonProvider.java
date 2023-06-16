package io.github.pulsebeat02.neon.json;

import com.google.gson.Gson;

public final class GsonProvider {

  public static final Gson GSON;

  static {
    GSON = new Gson();
  }

  public static Gson getGson() {
    return GSON;
  }
}
