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
package io.github.pulsebeat02.neon.utils;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

public final class MapUtils {

  private MapUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  @NotNull
  public static ItemStack getMapFromID(final int id) {
    final ItemStack map = createMapItemStack(id);
    final ItemMeta itemMeta = map.getItemMeta();
    itemMeta.setLore(List.of("Map ID [%s]".formatted(id)));
    map.setItemMeta(itemMeta);
    return map;
  }

  @SuppressWarnings("deprecation")
  private static @NotNull ItemStack createMapItemStack(final int id) {
    final ItemStack map = new ItemStack(Material.FILLED_MAP);
    final MapMeta mapMeta = requireNonNull((MapMeta) map.getItemMeta());
    mapMeta.setMapId(id);
    map.setItemMeta(mapMeta);
    return map;
  }

  public static void buildMapScreen(
      @NotNull final Player player,
      @NotNull final Material mat,
      final int width,
      final int height,
      final int startingMap) {
    checkNotNull(player, "Player cannot be null!");
    checkNotNull(mat, "Material cannot be null!");
    final World world = player.getWorld();
    final BlockFace face = player.getFacing();
    final BlockFace opposite = face.getOppositeFace();
    final Block start = player.getLocation().getBlock().getRelative(face);
    int map = startingMap;
    for (int h = height; h > 0; h--) {
      for (int w = 0; w < width; w++) {
        final Block current = getRelativeBlock(start, mat, w, h);
        final ItemFrame frame = getRelativeItemFrame(world, current, opposite, face, map);
        map++;
      }
    }
  }

  private static @NotNull ItemFrame getRelativeItemFrame(
      @NotNull final World world,
      @NotNull final Block current,
      @NotNull final BlockFace opposite,
      @NotNull final BlockFace face,
      final int map) {
    final ItemFrame frame =
        world.spawn(current.getRelative(opposite).getLocation(), ItemFrame.class);
    frame.setFacingDirection(face);
    frame.setItem(getMapFromID(map));
    frame.setInvulnerable(true);
    frame.setGravity(false);
    return frame;
  }

  private static @NotNull Block getRelativeBlock(
      @NotNull final Block start, @NotNull final Material mat, final int w, final int h) {
    final Block block = start.getRelative(BlockFace.UP, h).getRelative(BlockFace.EAST, w);
    block.setType(mat);
    return block;
  }
}
