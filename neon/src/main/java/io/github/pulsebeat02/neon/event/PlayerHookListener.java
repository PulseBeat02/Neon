package io.github.pulsebeat02.neon.event;

import io.github.pulsebeat02.neon.Neon;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerHookListener implements Listener {

  private @NotNull final Neon neon;

  public PlayerHookListener(@NotNull final Neon neon) {
    this.neon = neon;
    neon.getServer().getPluginManager().registerEvents(this, neon);
  }

  @EventHandler
  public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {
    final Player p = event.getPlayer();
    final UUID uuid = p.getUniqueId();
    this.neon.getPacketSender().injectPlayer(uuid);
  }

  @EventHandler
  public void onPlayerLeave(@NotNull final PlayerQuitEvent event) {
    final Player p = event.getPlayer();
    final UUID uuid = p.getUniqueId();
    this.neon.getPacketSender().uninjectPlayer(uuid);
  }
}
