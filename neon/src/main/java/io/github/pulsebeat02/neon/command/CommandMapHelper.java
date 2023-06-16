package io.github.pulsebeat02.neon;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

public final class CommandMapHelper {

  private static final SimpleCommandMap SIMPLE_COMMAND_MAP;

  static {
    try {
      final Class<?> clazz = Bukkit.getServer().getClass();
      final String name = "getCommandMap";
      final MethodType type = MethodType.methodType(SimpleCommandMap.class);
      SIMPLE_COMMAND_MAP =
          (SimpleCommandMap)
              MethodHandles.publicLookup()
                  .findVirtual(clazz, name, type)
                  .invoke(Bukkit.getServer());
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull SimpleCommandMap getCommandMap() {
    return SIMPLE_COMMAND_MAP;
  }
}
