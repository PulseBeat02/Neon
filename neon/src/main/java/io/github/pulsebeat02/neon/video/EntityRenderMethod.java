package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class EntityRenderMethod extends RenderAdapter {

  private @NotNull final PacketSender sender;
  private @NotNull final String character;
  private final int width;
  private final int height;
  private @NotNull final Location location;
  private @NotNull final Entity[] entities;

  public EntityRenderMethod(@NotNull final Neon neon, @NotNull final BrowserSettings settings) {
    super(neon);
    this.sender = neon.getPacketSender();
    this.character = settings.getCharacter();
    this.location = settings.getLocation();
    final ImmutableDimension dimension = settings.getResolution();
    this.width = dimension.getWidth();
    this.height = dimension.getHeight();
    this.entities = new Entity[this.height];
  }

  @Override
  public void destroy() {
    super.destroy();
    for (final Entity entity : this.entities) {
      entity.remove();
    }
  }

  public @NotNull String repeat(final int height) {
    return this.character.repeat(Math.max(0, height));
  }

  @Override
  public void render(@NotNull final IntBuffer buf) {
    this.sender.displayEntities(
        null, this.location, this.entities, buf, this.character, this.width, this.height);
  }

  public @NotNull Entity[] getEntities() {
    return this.entities;
  }
}
