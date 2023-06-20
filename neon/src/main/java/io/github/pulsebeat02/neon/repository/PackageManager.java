package io.github.pulsebeat02.neon.repository;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.cef.OS;
import org.jetbrains.annotations.NotNull;

public final class PackageManager {

  private @NotNull final Neon neon;
  private @NotNull final Path script;

  public PackageManager(@NotNull final Neon neon) throws IOException {
    this.neon = neon;
    this.script = neon.getDataFolder().toPath().resolve("notroot");
    if (OS.isLinux()) {
      this.copyScript();
      this.setExecutePermissions();
      this.installPackages();
    }
  }

  private void copyScript() throws IOException {
    try (final InputStream stream = ResourceUtils.getResourceAsStream("package/notroot")) {
      Files.copy(stream, this.script, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void setExecutePermissions() throws IOException {
    final Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx");
    Files.setPosixFilePermissions(this.script, ownerWritable);
  }

  private void installPackages() throws IOException {
    this.installPackageWithoutRoot("apt-rdepends");
    this.installPackageWithoutRoot("libxtst6");
  }

  private void installPackageWithoutRoot(@NotNull final String packageName) throws IOException {
    final ProcessBuilder builder =
        new ProcessBuilder(this.script.toAbsolutePath().toString(), "install", packageName);
    final Process p = builder.start();

    String line;
    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    BufferedReader rdr = new BufferedReader(isr);
    while ((line = rdr.readLine()) != null) {
      this.neon.logConsole("PACKAGE INSTALLER INFO: %s".formatted(line));
    }

    isr = new InputStreamReader(p.getErrorStream());
    rdr = new BufferedReader(isr);
    while ((line = rdr.readLine()) != null) {
      this.neon.logConsole("PACKAGE INSTALLER ERROR: %s".formatted(line));
    }

    try {
      p.waitFor();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
