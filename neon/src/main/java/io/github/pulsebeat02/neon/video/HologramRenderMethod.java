/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.utils.TaskUtils;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HologramRenderMethod extends EntityRenderMethod {

  private final int height;
  private final int width;
  private @NotNull final Location location;

  public HologramRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
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
      final Consumer<ArmorStand> handleEntity = this::handleEntity;
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
      final Consumer<ArmorStand> handleEntity,
      final int index) {
    entities[index] = world.spawn(spawn, ArmorStand.class, handleEntity);
    entities[index].setCustomName(this.repeat(this.height));
    entities[index].setCustomNameVisible(true);
    return null;
  }

  public void handleEntity(@NotNull final Entity entity) {
    final ArmorStand stand = (ArmorStand) entity;
    stand.setInvulnerable(true);
    stand.setVisible(false);
    stand.setGravity(false);
  }
}
