package io.github.pulsebeat02.neon.event;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

public final class BrowserClickListener implements Listener {

  private final Neon neon;

  public BrowserClickListener(@NotNull final Neon neon) {
    this.neon = neon;
  }

  @EventHandler
  private void onHangingBreakEvent(@NotNull final HangingBreakEvent event) {
    final Hanging hanging = event.getEntity();
    if (!(hanging instanceof final ItemFrame frame)) {
      return;
    }
    final ItemStack stack = frame.getItem();
    if (this.checkValidMap(stack)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  private void onPlayerInteractEntityEvent(@NotNull final PlayerInteractEntityEvent event) {
    final Entity entity = event.getRightClicked();
    if (!(entity instanceof ItemFrame frame)) {
      return;
    }
    final ItemStack stack = frame.getItem();
    if (this.checkValidMap(stack)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  private void onEntityDamageFrameEvent(@NotNull final EntityDamageEvent event) {
    final Entity entity = event.getEntity();
    if (!(entity instanceof ItemFrame frame)) {
      return;
    }
    final ItemStack stack = frame.getItem();
    if (this.checkValidMap(stack)) {
      event.setCancelled(true);
    }
  }

  private boolean checkValidMap(@NotNull final ItemStack stack) {
    final ItemMeta meta = stack.getItemMeta();
    if (!(meta instanceof final MapMeta mapMeta)) {
      return false;
    }
    final BrowserConfiguration configuration = this.neon.getConfiguration();
    final int width = configuration.getBlockWidth();
    final int height = configuration.getBlockHeight();
    final int max = width * height - 1;
    final int id = mapMeta.getMapId();
    return id <= max;
  }
}
