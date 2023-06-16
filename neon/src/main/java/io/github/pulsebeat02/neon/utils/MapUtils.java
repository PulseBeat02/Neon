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
    final ItemStack map = new ItemStack(Material.FILLED_MAP);
    final MapMeta mapMeta = requireNonNull((MapMeta) map.getItemMeta());
    mapMeta.setMapId(id);
    map.setItemMeta(mapMeta);
    final ItemMeta itemMeta = map.getItemMeta();
    itemMeta.setLore(List.of("Map ID [%s]".formatted(id)));
    map.setItemMeta(itemMeta);
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
        final Block current = start.getRelative(BlockFace.UP, h).getRelative(BlockFace.EAST, w);
        current.setType(mat);
        final ItemFrame frame =
            world.spawn(current.getRelative(opposite).getLocation(), ItemFrame.class);
        frame.setFacingDirection(face);
        frame.setItem(getMapFromID(map));
        map++;
      }
    }
  }
}
