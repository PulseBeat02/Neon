package io.github.pulsebeat02.neon.utils.immutable;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class ImmutableLocation {

  private final double x;
  private final double y;
  private final double z;

  public ImmutableLocation(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public double getZ() {
    return this.z;
  }

  public static @NotNull ImmutableLocation of(@NotNull final Location location) {
    final double x = location.getX();
    final double y = location.getY();
    final double z = location.getZ();
    return new ImmutableLocation(x, y, z);
  }
}
