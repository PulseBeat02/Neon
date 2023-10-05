package io.github.pulsebeat02.neon.locale;

import io.github.pulsebeat02.neon.Neon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AudienceHandler {
  private @Nullable final BukkitAudiences audience;

  public AudienceHandler(@NotNull final Neon neon) {
    this.audience = BukkitAudiences.create(neon);
  }

  public void shutdown() {
    if (this.audience != null) {
      this.audience.close();
    }
  }

  public @NotNull BukkitAudiences retrieve() {
    this.checkStatus();
    return this.audience;
  }

  public @NotNull Audience console() {
    this.checkStatus();
    return this.audience.console();
  }

  private void checkStatus() {
    if (this.audience == null) {
      throw new AssertionError("Tried to access Adventure when the plugin was disabled!");
    }
  }
}