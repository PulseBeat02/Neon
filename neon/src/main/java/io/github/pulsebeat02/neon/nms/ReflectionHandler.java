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
package io.github.pulsebeat02.neon.nms;

import io.github.pulsebeat02.neon.Neon;

import io.github.pulsebeat02.neon.locale.Locale;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class ReflectionHandler {
  private static @NotNull final String VERSION;

  static {
    VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  }

  private @NotNull final Neon neon;

  public ReflectionHandler(@NotNull final Neon neon) {
    this.neon = neon;
  }

  private static @NotNull PacketSender getPacketHandler() throws Exception {
    return (PacketSender) getPacketHandlerClass().getDeclaredConstructor().newInstance();
  }

  private static @NotNull Class<?> getPacketHandlerClass() throws ClassNotFoundException {
    return Class.forName("io.github.pulsebeat02.neon.nms.%s.NeonPacketSender".formatted(VERSION));
  }

  public static @NotNull String getVersion() {
    return VERSION;
  }

  public @NotNull PacketSender getNewPacketHandlerInstance() {
    try {
      return getPacketHandler();
    } catch (final Exception e) {
      final String software = this.neon.getServer().getVersion();
      throw new AssertionError(
          "Current server implementation (%s) is not supported!".formatted(software));
    }
  }
}
