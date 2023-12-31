/*
 * MIT License
 *
 * Copyright (c) 2024 Brandon Li
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
package io.github.pulsebeat02.neon.event;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.browser.MouseClick;
import io.github.pulsebeat02.neon.browser.SeleniumBrowser;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class BrowserClickListener implements Listener {

  private @NotNull final Neon neon;
  private final long lastInteraction;

  public BrowserClickListener(@NotNull final Neon neon) {
    this.neon = neon;
    this.lastInteraction = System.currentTimeMillis();
    neon.getServer().getPluginManager().registerEvents(this, neon);
  }

  public static @NotNull Optional<Location> calculateVector(
      @NotNull final Location planeLoc,
      @NotNull final Vector plane,
      @NotNull final Location origin,
      @NotNull final Vector direction) {
    if (plane.dot(direction) == 0) {
      return Optional.empty();
    }
    final double distance =
        (plane.dot(planeLoc.toVector()) - plane.dot(origin.toVector())) / plane.dot(direction);
    return Optional.of(origin.clone().add(direction.multiply(distance)));
  }

  @EventHandler
  private void onHangingBreakEvent(@NotNull final HangingBreakEvent event) {
    final Hanging hanging = event.getEntity();
    if (!(hanging instanceof final ItemFrame frame)) {
      return;
    }
    final ItemStack stack = frame.getItem();
    if (!this.checkValidMap(stack)) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  private void onEntityDamageFrameEvent(@NotNull final EntityDamageEvent event) {

    final Entity entity = event.getEntity();
    if (!(entity instanceof final ItemFrame frame)) {
      return;
    }

    final ItemStack stack = frame.getItem();
    if (!this.checkValidMap(stack)) {
      return;
    }
    event.setCancelled(true);

    if (!(event instanceof final EntityDamageByEntityEvent damageEvent)) {
      return;
    }

    final Entity damager = damageEvent.getDamager();
    if (!(damager instanceof final Player player)) {
      return;
    }

    this.handleInteraction(player, frame, false);
  }

  @EventHandler
  private void onPlayerInteractEntityEvent(@NotNull final PlayerInteractEntityEvent event) {

    final Entity entity = event.getRightClicked();
    if (!(entity instanceof final ItemFrame frame)) {
      return;
    }

    final ItemStack stack = frame.getItem();
    if (!this.checkValidMap(stack)) {
      return;
    }
    event.setCancelled(true);

    final Player player = event.getPlayer();
    this.handleInteraction(player, frame, true);
  }

  public void handleInteraction(
      @NotNull final Player player, @NotNull final ItemFrame frame, final boolean right) {
    final long now = System.currentTimeMillis();
    if ((this.lastInteraction + 100) > now) {
      return;
    }
    final Location eye = player.getEyeLocation();
    final Vector direction = player.getLocation().getDirection();
    final int[] coords = this.getBoardCoords(frame, eye, direction);
    final int x = coords[0];
    final int y = coords[1];
    final SeleniumBrowser browser = this.neon.getBrowser();
    final MouseClick type = right ? MouseClick.RIGHT : MouseClick.LEFT;
    browser.sendMouseEvent(x, y, type);
  }

  /*
  Stolen from https://github.com/BananaPuncher714/RadioBoard/blob/master/src/io/github/bananapuncher714/radioboard/BoardListener.java
   */
  private int[] getBoardCoords(
      @NotNull final ItemFrame frame,
      @NotNull final Location origin,
      @NotNull final Vector direction) {

    final Location topLeft = frame.getLocation().getBlock().getLocation();
    final Location originLocation = origin.clone();
    final Vector originDirection = direction.clone();
    if (originLocation.getWorld() != topLeft.getWorld()) {
      return new int[] {};
    }

    final BlockFace face = frame.getAttachedFace();
    final Vector normal = new Vector(face.getModX(), face.getModY(), face.getModZ());
    if (normal.dot(originDirection) < 0) {
      return new int[] {};
    }

    final Vector positive = normal.clone();
    positive.multiply(positive).multiply(8 / 16.0);
    positive.add(normal.clone().multiply(6.9 / 16.0));

    final Location point = topLeft.clone().add(positive);
    final Optional<Location> optional =
        calculateVector(point, normal, originLocation, originDirection);
    if (optional.isEmpty()) {
      return new int[] {};
    }

    final Location location = optional.get();
    location.subtract(location.getBlockX(), location.getBlockY(), location.getBlockZ());

    final int x;
    final int y;
    if (normal.getX() != 0) {
      x = (int) Math.abs((location.getZ() - (.5 - .5 * normal.getX())) * 128);
      y = 127 - (int) (location.getY() * 128);
    } else if (normal.getY() != 0) {
      x = (int) (location.getZ() * 128);
      y = 127 - (int) (location.getX() * 128);
    } else {
      x = (int) Math.abs((location.getX() - (.5 + .5 * normal.getZ())) * 128);
      y = 127 - (int) (location.getY() * 128);
    }

    final ItemStack stack = frame.getItem();
    final ItemMeta meta = stack.getItemMeta();
    final MapMeta mapMeta = (MapMeta) meta;
    final int id = mapMeta.getMapId();

    final SeleniumBrowser browser = this.neon.getBrowser();
    final BrowserSettings settings = browser.getSettings();
    final ImmutableDimension blockDimension = settings.getBlockDimension();
    final int blockWidth = blockDimension.getWidth();
    final int gridX = (id - 1) % blockWidth;
    final int gridY = (id - 1) / blockWidth;
    final int trueX = x + (gridX * 128);
    final int trueY = y + (gridY * 128);

    return new int[] {trueX, trueY};
  }

  private boolean checkValidMap(@NotNull final ItemStack stack) {
    final ItemMeta meta = stack.getItemMeta();
    if (!(meta instanceof final MapMeta mapMeta)) {
      return false;
    }
    final BrowserConfiguration configuration = this.neon.getConfiguration();
    final ImmutableDimension dimension = configuration.getBlockDimension();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    final int max = width * height - 1;
    final int id = mapMeta.getMapId();
    return id <= max;
  }
}
