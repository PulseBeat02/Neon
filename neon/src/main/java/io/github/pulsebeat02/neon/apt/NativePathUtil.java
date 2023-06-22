package io.github.pulsebeat02.neon.apt;

import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.burningwave.core.classes.Modules;
import org.jetbrains.annotations.NotNull;

public final class NativePathUtil {

  private static final Field USER_PATHS;

  static {
    Modules.create().exportAllToAll();
    try {
      final Class<?>[] outer = getNativeLibrariesDeclaredClasses();
      final Class<?> libraryPaths = getInnerStaticClass(outer);
      USER_PATHS = libraryPaths.getDeclaredField("USER_PATHS");
      USER_PATHS.setAccessible(true);
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }

  private NativePathUtil() {
    throw new AssertionError("Utility class cannot be instantiated");
  }

  public static Class<?> @NotNull [] getNativeLibrariesDeclaredClasses()
      throws ClassNotFoundException {
    return Class.forName("jdk.internal.loader.NativeLibraries").getDeclaredClasses();
  }

  public static @NotNull Class<?> getInnerStaticClass(@NotNull final Class<?>[] outer) {
    return Arrays.stream(outer)
        .filter(klass -> klass.getSimpleName().equals("LibraryPaths"))
        .findFirst()
        .orElseThrow();
  }

  public static void addNativeLibraryPath(@NotNull final String path)
      throws IllegalAccessException {
    final String[] tmp = getUpdatedPaths(path);
    UnsafeUtils.setStaticFinalField(USER_PATHS, tmp);
  }

  @NotNull
  private static String @NotNull [] getUpdatedPaths(@NotNull final String path)
      throws IllegalAccessException {
    final String[] paths = (String[]) USER_PATHS.get(null);
    final String[] tmp = new String[paths.length + 1];
    System.arraycopy(paths, 0, tmp, 0, paths.length);
    tmp[paths.length] = path;
    return tmp;
  }
}
