package io.github.pulsebeat02.neon.locale;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface Sender {

  void sendMessage(@NotNull final Component component);

  boolean hasPermission(@NotNull final String permission);
}
