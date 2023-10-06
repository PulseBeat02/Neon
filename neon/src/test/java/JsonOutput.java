import io.github.pulsebeat02.neon.utils.JsonUtils;

import java.io.IOException;

public class JsonOutput {

  public static void main(final String[] args) throws IOException {
    System.out.println(JsonUtils.toListFromResource("command/suggestions/blockdim.json"));
    System.out.println(JsonUtils.toListFromResource("command/suggestions/rendertype.json"));
    System.out.println(JsonUtils.toListFromResource("command/suggestions/resolution.json"));
    System.out.println(JsonUtils.toMapFromResource("command/suggestions/character.json"));
  }
}
