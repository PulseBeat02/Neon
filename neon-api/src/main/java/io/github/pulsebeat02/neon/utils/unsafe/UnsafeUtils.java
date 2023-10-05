package io.github.pulsebeat02.neon.utils.unsafe;

import java.lang.reflect.Field;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

public final class UnsafeUtils {

  private static @NotNull final Unsafe UNSAFE;

  static {
    UNSAFE = UnsafeProvider.getUnsafe();
  }

  private UnsafeUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static void setFinalField(
      @NotNull final Field field, @NotNull final Object obj, @Nullable final Object value) {
    UNSAFE.putObject(obj, UNSAFE.objectFieldOffset(field), value);
  }

  public static void setStaticFinalField(@NotNull final Field field, @Nullable final Object value) {
    UNSAFE.putObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field), value);
  }

  public static @NotNull Object getFieldExceptionally(
      @NotNull final Object object, @NotNull final String name) {
    try {
      return getField(object, name);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull Object getFieldExceptionally(
      @NotNull final Class<?> clazz, @NotNull final Object object, @NotNull final String name) {
    try {
      return getField(clazz, object, name);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull Object getField(@NotNull final Object object, @NotNull final String name)
      throws NoSuchFieldException {
    return getField(object.getClass(), object, name);
  }

  public static @NotNull Object getField(
      @NotNull final Class<?> clazz, @NotNull final Object object, @NotNull final String name)
      throws NoSuchFieldException {
    return UNSAFE.getObject(object, UNSAFE.objectFieldOffset(clazz.getDeclaredField(name)));
  }

  public static void setEnvironmentalVariable(@NotNull final String key, @NotNull final String value) {
    try {
      final Map<String, String> unwritable = System.getenv();
      final Map<String, String> writable =
              (Map<String, String>) UnsafeUtils.getField(unwritable, "m");
      writable.put(key, value);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }
}
