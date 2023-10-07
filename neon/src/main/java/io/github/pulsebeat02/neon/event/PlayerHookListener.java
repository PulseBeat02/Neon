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
