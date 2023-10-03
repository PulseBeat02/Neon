package io.github.pulsebeat02.neon.locale;

import io.github.pulsebeat02.neon.Neon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;

public final class AudienceHandler {
  private final BukkitAudiences audience;

  public AudienceHandler(@NotNull final Neon neon) {
    this.audience = BukkitAudiences.create(neon);
  }

  public void shutdown() {
    if (this.audience != null) {
      this.audience.close();
    }
  }

  public @NotNull BukkitAudiences retrieve() {
    if (this.audience == null) {
      throw new IllegalStateException("Tried to access BukkitAudiences when the plugin was disabled!");
    }
    return this.audience;
  }

  public @NotNull Audience console() {
    if (this.audience == null) {
      throw new IllegalStateException("Tried to access Console when the plugin was disabled!");
    }
    return audience.console();
  }
}
