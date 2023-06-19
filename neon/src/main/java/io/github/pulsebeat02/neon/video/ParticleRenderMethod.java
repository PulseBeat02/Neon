package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

public final class ParticleRenderMethod extends EntityRenderMethod {

  public ParticleRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon, settings);
  }

  @Override
  public @NotNull Class<?> getEntityClass() {
    return EntityType.AREA_EFFECT_CLOUD.getEntityClass();
  }

  @Override
  public @NotNull Consumer<?> handleEntity() {
    return (entity) -> {
      final AreaEffectCloud cloud = (AreaEffectCloud) entity;
      cloud.setInvulnerable(true);
      cloud.setDuration(999999);
      cloud.setDurationOnUse(0);
      cloud.setRadiusOnUse(0);
      cloud.setRadius(0);
      cloud.setRadiusPerTick(0);
      cloud.setReapplicationDelay(0);
      cloud.setGravity(false);
    };
  }
}
