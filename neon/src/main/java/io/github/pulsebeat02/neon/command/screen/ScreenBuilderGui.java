package io.github.pulsebeat02.neon.command.screen;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.pulsebeat02.neon.utils.MapUtils;
import io.github.pulsebeat02.neon.utils.gui.ItemBuilder;
import io.github.pulsebeat02.neon.utils.gui.SkullCreator;
import io.github.pulsebeat02.neon.utils.mutable.MutableInt;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ScreenBuilderGui {

  private static @NotNull final String INCREASE_BASE64;
  private static @NotNull final String DECREASE_BASE64;

  static {
    INCREASE_BASE64 =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWNkYjhmNDM2NTZjMDZjNGU4NjgzZTJlNjM0MWI0NDc5ZjE1N2Y0ODA4MmZlYTRhZmYwOWIzN2NhM2M2OTk1YiJ9fX0=";
    DECREASE_BASE64 =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlMWU3MzBjNzcyNzljOGUyZTE1ZDhiMjcxYTExN2U1ZTJjYTkzZDI1YzhiZTNhMDBjYzkyYTAwY2MwYmI4NSJ9fX0=";
  }

  private @NotNull final ChestGui gui;
  private @NotNull final StaticPane pane;
  private @NotNull final Player viewer;
  private @NotNull final MutableInt width;
  private @NotNull final MutableInt height;
  private @NotNull Material material;

  public ScreenBuilderGui(@NotNull final Player player) {
    this.gui = new ChestGui(5, "Choose Screen Size");
    this.pane = new StaticPane(9, 5);
    this.material = Material.OAK_PLANKS;
    this.viewer = player;
    this.width = new MutableInt(5);
    this.height = new MutableInt(5);
    this.initialize();
    this.gui.show(player);
  }

  // ____________________________
  // │__│__│__│__│__│__│__│__│__│
  // │__│XX│__│XX│__│XX│__│__│__│
  // │__│__│__│__│__│__│__│XX│__│
  // │__│XX│__│XX│__│XX│__│__│__│
  // │__│__│__│__│__│__│__│__│__│
  // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾

  private void initialize() {
    this.gui.setOnGlobalClick(this::cancelEvent);
    this.pane.addItem(this.getBuildScreenItem(), 7, 2);
    this.pane.addItem(
        this.getGuiItem(this.getIncreaseArrow("Block Width"), this.width, true), 1, 1);
    this.pane.addItem(
        this.getGuiItem(this.getDecreaseArrow("Block Width"), this.width, false), 1, 3);
    this.pane.addItem(
        this.getGuiItem(this.getIncreaseArrow("Block Height"), this.height, true), 3, 1);
    this.pane.addItem(
        this.getGuiItem(this.getIncreaseArrow("Block Height"), this.height, false), 3, 3);
    this.gui.addPane(this.pane);
    this.update();
  }

  private void cancelEvent(@NotNull final InventoryClickEvent event) {
    if (event.getClickedInventory() == null) {
      return;
    }
    if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
      return;
    }
    event.setCancelled(true);
  }

  private @NotNull GuiItem getBuildScreenItem() {
    return ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
        .name(text("Build Screen", GREEN))
        .action(this::handleBuildScreen)
        .build();
  }

  private void handleBuildScreen(@NotNull final InventoryClickEvent event) {
    this.viewer.closeInventory();
    MapUtils.buildMapScreen(
        this.viewer, this.material, this.width.getNumber(), this.height.getNumber(), 0);
    this.viewer.playSound(this.viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
  }

  private @NotNull GuiItem getGuiItem(
      @NotNull final ItemStack stack, @NotNull final MutableInt update, final boolean add) {
    return new GuiItem(stack, event -> this.mutateValue(update, add));
  }

  private void mutateValue(@NotNull final MutableInt update, final boolean add) {
    if (add) {
      update.increment();
    } else {
      update.decrement();
    }
    this.update();
  }

  private void update() {
    this.pane.addItem(this.getWidthItem(), 1, 2);
    this.pane.addItem(this.getHeightItem(), 3, 2);
    this.pane.addItem(this.getMaterialItem(), 7, 2);
    this.gui.update();
  }

  public @NotNull GuiItem getMaterialItem() {
    return ItemBuilder.from(this.material)
        .name(join(noSeparators(), text("Material - ", GOLD), text(this.material.toString(), AQUA)))
        .action(this::handleMaterial)
        .build();
  }

  private void handleMaterial(@NotNull final InventoryClickEvent event) {
    final ItemStack stack = event.getCursor();
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    this.material = stack.getType();
    final Component name =
        join(noSeparators(), text("Material - ", GOLD), text(this.material.toString(), AQUA));
    final GuiItem newStack = ItemBuilder.from(this.material).name(name).build();
    this.pane.addItem(newStack, 7, 2);
    this.gui.update();
  }

  public @NotNull GuiItem getWidthItem() {
    return ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
        .name(text("Screen Width (%d Blocks)".formatted(this.width.getNumber()), GOLD))
        .build();
  }

  public @NotNull GuiItem getHeightItem() {
    return ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
        .name(text("Screen Height (%d Blocks)".formatted(this.height.getNumber()), GOLD))
        .build();
  }

  private @NotNull ItemStack getIncreaseArrow(@NotNull final String data) {
    return ItemBuilder.from(SkullCreator.itemFromBase64(INCREASE_BASE64))
        .name(text("Increase %s by One".formatted(data), GREEN))
        .buildWithoutAction();
  }

  private @NotNull ItemStack getDecreaseArrow(@NotNull final String data) {
    return ItemBuilder.from(SkullCreator.itemFromBase64(DECREASE_BASE64))
        .name(text("Decrease %s by One".formatted(data), RED))
        .buildWithoutAction();
  }
}
