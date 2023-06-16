package io.github.pulsebeat02.neon.utils;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;
import static org.bukkit.ChatColor.translateAlternateColorCodes;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class ItemBuilder {

  private final ItemStack is;
  private Consumer<InventoryClickEvent> action;

  ItemBuilder(@NotNull final ItemStack is) {
    this.is = is;
  }

  @Contract("_ -> new")
  public static @NotNull ItemBuilder from(@NotNull final Material material) {
    return from(material, 1);
  }

  @Contract("_, _ -> new")
  public static @NotNull ItemBuilder from(@NotNull final Material material, final int count) {
    return from(new ItemStack(material, count));
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull ItemBuilder from(@NotNull final ItemStack stack) {
    return new ItemBuilder(stack);
  }

  public @NotNull ItemBuilder durability(final short dur) {
    this.is.setDurability(dur);
    return this;
  }

  public @NotNull ItemBuilder name(@NotNull final Component name) {
    final ItemMeta im = this.is.getItemMeta();
    im.setDisplayName(translateAlternateColorCodes('&', legacyAmpersand().serialize(name)));
    this.is.setItemMeta(im);
    return this;
  }

  public @NotNull ItemBuilder action(@NotNull final Consumer<InventoryClickEvent> action) {
    this.action = action;
    return this;
  }

  public @NotNull GuiItem build() {
    return new GuiItem(this.is, this.action);
  }

  public @NotNull ItemStack buildWithoutAction() {
    return this.is;
  }
}
