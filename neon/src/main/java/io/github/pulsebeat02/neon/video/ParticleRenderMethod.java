package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

public final class ParticleRenderMethod extends EntityRenderMethod {

  private final int height;
  private @NotNull final Location location;

  public ParticleRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon, settings);
    this.location = settings.getLocation();
    final ImmutableDimension dimension = settings.getResolution();
    this.height = dimension.getHeight();
  }

  @Override
  public void setup() {
    super.setup();
    final Location spawn = this.location.clone();
    final World world = spawn.getWorld();
    final Entity[] entities = this.getEntities();
    final Neon neon = this.getNeon();
    for (int i = this.height - 1; i >= 0; i--) {
      final Consumer<AreaEffectCloud> handleEntity = this::handleEntity;
      final int index = i;
      new BukkitRunnable() {
        @Override
        public void run() {
          entities[index] = world.spawn(spawn, AreaEffectCloud.class, handleEntity);
        }
      }.runTask(neon);
      entities[i].setCustomName(this.repeat(this.height));
      entities[i].setCustomNameVisible(true);
      spawn.add(0.0, 0.225, 0.0);
    }
  }

  public void handleEntity(@NotNull final Entity entity) {
    final AreaEffectCloud cloud = (AreaEffectCloud) entity;
    cloud.setInvulnerable(true);
    cloud.setDuration(999999);
    cloud.setDurationOnUse(0);
    cloud.setRadiusOnUse(0);
    cloud.setRadius(0);
    cloud.setRadiusPerTick(0);
    cloud.setReapplicationDelay(0);
    cloud.setGravity(false);
  }
}
