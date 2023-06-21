package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

public final class HologramRenderMethod extends EntityRenderMethod {

  private final int height;
  private @NotNull final Location location;

  public HologramRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
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
      final Consumer<ArmorStand> handleEntity = this::handleEntity;
      final int index = i;
      new BukkitRunnable() {
        @Override
        public void run() {
          entities[index] = world.spawn(spawn, ArmorStand.class, handleEntity);
        }
      }.runTask(neon);
      entities[i].setCustomName(this.repeat(this.height));
      entities[i].setCustomNameVisible(true);
      spawn.add(0.0, 0.225, 0.0);
    }
  }

  public void handleEntity(@NotNull final Entity entity) {
    final ArmorStand stand = (ArmorStand) entity;
    stand.setInvulnerable(true);
    stand.setVisible(false);
    stand.setGravity(false);
  }
}
