package io.github.pulsebeat02.neon.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jetbrains.annotations.NotNull;

public final class ZipUtils {

  private ZipUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static void unzip(final Path source, final Path target) throws IOException {
    try (final ZipInputStream zis = new ZipInputStream(Files.newInputStream(source))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        final boolean isDirectory = zipEntry.getName().endsWith(File.separator);
        final Path newPath = zipSlipProtect(zipEntry, target);
        if (isDirectory) {
          Files.createDirectories(newPath);
        } else {
          handleFile(newPath);
          Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
    }
  }

  private static void handleFile(@NotNull final Path newPath) throws IOException {
    if (newPath.getParent() != null) {
      if (Files.notExists(newPath.getParent())) {
        Files.createDirectories(newPath.getParent());
      }
    }
  }

  private static @NotNull Path zipSlipProtect(
      @NotNull final ZipEntry zipEntry, @NotNull final Path targetDir) throws IOException {
    final Path targetDirResolved = targetDir.resolve(zipEntry.getName());
    final Path normalizePath = targetDirResolved.normalize();
    if (!normalizePath.startsWith(targetDir)) {
      throw new IOException("Bad zip entry: " + zipEntry.getName());
    }
    return normalizePath;
  }
}
