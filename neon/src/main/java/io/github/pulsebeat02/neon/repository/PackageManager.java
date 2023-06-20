package io.github.pulsebeat02.neon.repository;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Scanner;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class PackageManager {

  private @NotNull final Neon neon;
  private @NotNull final Path script;

  public PackageManager(@NotNull final Neon neon) throws IOException {
    this.neon = neon;
    this.script = neon.getDataFolder().toPath().resolve("pget");
    this.copyScript();
    this.setExecutePermissions();
    this.installPackages();
  }

  private void copyScript() throws IOException {
    try (final InputStream stream = ResourceUtils.getResourceAsStream("package/pget")) {
      Files.copy(stream, this.script, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void setExecutePermissions() throws IOException {
    final Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx");
    Files.setPosixFilePermissions(this.script, ownerWritable);
  }

  private void installPackages() throws IOException {
    this.installPackageWithoutRoot("libxtst6");
  }

  private void installPackageWithoutRoot(@NotNull final String packageName) throws IOException {
    final ProcessBuilder builder =
        new ProcessBuilder(this.script.toAbsolutePath().toString(), packageName);
    final Process p = builder.start();
    try (final InputStream inputStream = p.getInputStream();
        final Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
      final String result = s.hasNext() ? s.next() : null;
      if (result != null) {
        this.neon.logConsole("PACKAGE INSTALLATION SCRIPT: %s".formatted(result));
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    try {
      p.waitFor();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
