package io.github.pulsebeat02.neon.dither;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.neon.json.GsonProvider;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class MapPalette {

  public static final Color[] NMS_PALETTE;

  static {
    final Gson gson = GsonProvider.getGson();
    try (final Reader reader = ResourceUtils.getResourceAsReader("colors/colors.json")) {
      final TypeToken<int[][]> token = new TypeToken<>() {};
      final int[][] colors = gson.fromJson(reader, token.getType());
      NMS_PALETTE =
          Stream.of(colors)
              .map(color -> new Color(color[0], color[1], color[2]))
              .toArray(Color[]::new);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void init() {}

  @NotNull
  public static Color getColor(final byte val) {
    return NMS_PALETTE[val];
  }
}
