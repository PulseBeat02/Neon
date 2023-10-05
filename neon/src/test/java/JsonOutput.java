import com.google.gson.Gson;
import io.github.pulsebeat02.neon.json.GsonProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class JsonOutput {
  private static @NotNull final List<String> RESOLUTION_SUGGESTIONS;
  private static @NotNull final List<String> BLOCK_DIMENSION_SUGGESTIONS;
  private static @NotNull final List<String> BROWSER_RENDER_TYPE_SUGGESTIONS;
  private static @NotNull final Map<String, String> CHARACTER_SUGGESTIONS;

  static {
    RESOLUTION_SUGGESTIONS =
        List.of(
            "360x640",
            "375x667",
            "414x896",
            "360x780",
            "375x812",
            "1366x768",
            "1920x1080",
            "1536x864",
            "1440x900",
            "1280x720",
            "3840x2160");
    BLOCK_DIMENSION_SUGGESTIONS =
        List.of("1x1", "1x2", "3x3", "3x5", "5x5", "6x10", "8x14", "10x14", "8x8");
    CHARACTER_SUGGESTIONS =
        Map.of(
            "SMALL_SQUARE",
            "▪",
            "BIG_SQUARE",
            "■",
            "CIRCLE",
            "●",
            "HORIZONTAL_RECTANGLE",
            "▬",
            "VERTICAL_RECTANGLE",
            "▮",
            "SMILEY",
            "☺",
            "FROWNEY",
            "☹");
    BROWSER_RENDER_TYPE_SUGGESTIONS = List.of("MAP", "ENTITY");
  }

  public static void main(final String[] args) {
    final Gson gson = GsonProvider.getGson();
    System.out.println(gson.toJson(RESOLUTION_SUGGESTIONS));
    System.out.println(gson.toJson(BLOCK_DIMENSION_SUGGESTIONS));
    System.out.println(gson.toJson(BROWSER_RENDER_TYPE_SUGGESTIONS));
    System.out.println(gson.toJson(CHARACTER_SUGGESTIONS));
  }
}
