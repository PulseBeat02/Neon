package io.github.pulsebeat02.neon.command.browser.configure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum EntitySelection {
  HOLOGRAM,
  PARTICLE;
  private static @NotNull final Map<String, EntitySelection> KEY_LOOKUP;

  static {
    KEY_LOOKUP = new HashMap<>();
    for (final EntitySelection selection : EntitySelection.values()) {
      KEY_LOOKUP.put(selection.name(), selection);
    }
  }

  public static @NotNull Optional<EntitySelection> ofKey(@NotNull final String key) {
    return Optional.ofNullable(KEY_LOOKUP.get(key));
  }
}
