package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.netty.buffer.ByteBuf;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

public abstract class EntityRenderMethod<T extends Entity> extends RenderAdapter {

  private @NotNull final PacketSender sender;
  private @NotNull final String character;
  private final int width;
  private final int height;
  private @NotNull final Location location;
  private @NotNull Entity[] entities;

  public EntityRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon);
    this.sender = neon.getPacketSender();
    this.character = settings.getCharacter();
    this.location = settings.getLocation();
    final ImmutableDimension dimension = settings.getResolution();
    this.width = dimension.getWidth();
    this.height = dimension.getHeight();
  }

  @Override
  public void setup() {
    super.setup();
    final Location spawn = this.location.clone();
    final World world = spawn.getWorld();
    final Class<T> clazz = this.getEntityClass();
    for (int i = this.height - 1; i >= 0; i--) {
      final Consumer<T> handleEntity = this.handleEntity();
      this.entities[i] = world.spawn(spawn, clazz, handleEntity);
      this.entities[i].setCustomName(this.repeat(this.height));
      this.entities[i].setCustomNameVisible(true);
      spawn.add(0.0, 0.225, 0.0);
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    for (final Entity entity : this.entities) {
      entity.remove();
    }
  }

  public abstract @NotNull Class<T> getEntityClass();

  public abstract @NotNull Consumer<T> handleEntity();

  private @NotNull String repeat(final int height) {
    return this.character.repeat(Math.max(0, height));
  }

  @Override
  public void render(@NotNull final ByteBuf buf) {
    this.sender.displayEntities(
        null, this.location, this.entities, buf, this.character, this.width, this.height);
  }
}
