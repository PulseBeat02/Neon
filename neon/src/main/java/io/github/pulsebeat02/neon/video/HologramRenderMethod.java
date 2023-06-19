package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

public final class HologramRenderMethod extends EntityRenderMethod {

  public HologramRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon, settings);
  }

  @Override
  public @NotNull Class<?> getEntityClass() {
    return EntityType.ARMOR_STAND.getEntityClass();
  }

  @Override
  public @NotNull Consumer<?> handleEntity() {
    return (entity) -> {
      final ArmorStand stand = (ArmorStand) entity;
      stand.setInvulnerable(true);
      stand.setVisible(false);
      stand.setGravity(false);
    };
  }
}
