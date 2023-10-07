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
package io.github.pulsebeat02.neon.utils.item;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;
import static org.bukkit.ChatColor.translateAlternateColorCodes;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemBuilder {

  private @NotNull final ItemStack is;
  private @Nullable Consumer<InventoryClickEvent> action;

  ItemBuilder(@NotNull final ItemStack is) {
    this.is = is;
  }

  public static @NotNull ItemBuilder from(@NotNull final Material material) {
    return from(new ItemStack(material, 1));
  }

  public static @NotNull ItemBuilder from(@NotNull final ItemStack stack) {
    return new ItemBuilder(stack);
  }

  public @NotNull ItemBuilder name(@NotNull final Component name) {
    final String message = translateAlternateColorCodes('&', legacyAmpersand().serialize(name));
    final ItemMeta im = this.is.getItemMeta();
    im.setDisplayName(message);
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
