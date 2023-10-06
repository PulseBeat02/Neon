/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.neon.video;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.browser.BrowserSettings;
import io.github.pulsebeat02.neon.nms.PacketSender;
import io.github.pulsebeat02.neon.utils.immutable.ImmutableDimension;

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
