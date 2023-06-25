package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.utils.TaskUtils;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.concurrent.ExecutionException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParticleRenderMethod extends EntityRenderMethod {

  private final int height;
  private final int width;
  private @NotNull final Location location;

  public ParticleRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon, settings);
    this.location = settings.getLocation();
    final ImmutableDimension dimension = settings.getResolution();
    this.height = dimension.getHeight();
    this.width = dimension.getWidth();
  }

  @Override
  public void setup() {
    super.setup();
    final Location spawn = this.location.clone();
    final World world = spawn.getWorld();
    final Entity[] entities = this.getEntities();
    final Neon neon = this.getNeon();
    for (int i = this.width - 1; i >= 0; i--) {
      final Consumer<AreaEffectCloud> handleEntity = this::handleEntity;
      final int index = i;
      TaskUtils.sync(neon, () -> this.spawnEntity(spawn, world, entities, handleEntity, index));
      spawn.add(0.0, 0.1, 0.0);
    }
  }

  @Nullable
  private Void spawnEntity(
      @NotNull final Location spawn,
      @NotNull final World world,
      @NotNull final Entity[] entities,
      final Consumer<AreaEffectCloud> handleEntity,
      final int index) {
    entities[index] = world.spawn(spawn, AreaEffectCloud.class, handleEntity);
    entities[index].setCustomName(this.repeat(this.height));
    entities[index].setCustomNameVisible(true);
    return null;
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
