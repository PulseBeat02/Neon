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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class PackageManager {

  private static final Set<String> JCEF_DEPENDENCIES;

  static {
    // some os's don't even have some of these bare-bones dependencies...
    JCEF_DEPENDENCIES = Set.of("libxtst6", "libxi6", "libnss3-tools");
  }

  private @NotNull final Neon neon;
  private @NotNull final Path folder;
  private @NotNull final Path script;

  public PackageManager(@NotNull final Neon neon) throws IOException {
    this.neon = neon;
    this.folder = neon.getDataFolder().toPath().resolve("apt");
    this.script = this.folder.resolve("notroot");
    if (this.isUnix()) {
      this.createFolders();
      this.copyScript();
      this.setExecutePermissions();
      this.installPackages();
      this.loadLibraries();
    }
  }

  private boolean isUnix() {
    final String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return OS.contains("nux");
  }

  private void createFolders() throws IOException {
    if (!Files.isDirectory(this.folder)) {
      Files.createDirectories(this.folder);
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

  private void installPackages() {
    JCEF_DEPENDENCIES.forEach(this::installPackage);
  }

  private void installPackage(@NotNull final String name) {
    try {
      this.installPackageWithoutRoot(name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
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
      isr.close();
      rdr.close();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadLibraries() throws IOException {
    final Path libs = this.folder.resolve("usr").resolve("lib");
    try (final Stream<Path> folder = Files.list(libs)) {
      final Path first = this.findFirstArchFolder(folder);
      this.handleLibrarySo(first);
    }
  }

  private void handleLibrarySo(@NotNull final Path first) throws IOException {
    try (final Stream<Path> files = Files.list(first)) {
      final Set<Path> set = this.findFile(files);
      for (final Path path : set) {
        final Path absolute = path.toAbsolutePath();
        final String nativePath = absolute.toString();
        this.neon.logConsole("Loading Native Library: %s".formatted(nativePath));
        System.load(nativePath);
      }
    }
  }

  private @NotNull Path findFirstArchFolder(@NotNull final Stream<Path> stream) {
    return stream.filter(Files::isDirectory).findFirst().orElseThrow();
  }

  private @NotNull Set<Path> findFile(@NotNull final Stream<Path> stream) {
    return stream
        .parallel()
        .filter(file -> file.getFileName().toString().contains(".so"))
        .collect(Collectors.toUnmodifiableSet());
  }
}
