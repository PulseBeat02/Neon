package io.github.pulsebeat02.neon.nms;

import io.github.pulsebeat02.neon.Neon;
import java.lang.reflect.InvocationTargetException;
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
      this.neon.logConsole("Unsupported Server Software %s!".formatted(software));
      throw new AssertionError(
          "Current server implementation (%s) is not supported!".formatted(software));
    }
  }
}
