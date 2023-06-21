package io.github.pulsebeat02.neon.repository;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.utils.ProcessUtils;
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

  private @NotNull final Neon neon;
  private @NotNull final Path folder;
  private @NotNull final Path script;

  public PackageManager(@NotNull final Neon neon) throws IOException {
    this.neon = neon;
    this.folder = neon.getDataFolder().toPath().resolve("apt");
    this.script = this.folder.resolve("install");
    if (this.isUnix()) {
      this.createFolders();
      this.copyScripts();
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

  private void copyScripts() throws IOException {
    try (final InputStream stream = ResourceUtils.getResourceAsStream("package/install")) {
      Files.copy(stream, this.script, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void setExecutePermissions() throws IOException {
    final Set<PosixFilePermission> all = PosixFilePermissions.fromString("rwxrwxrwx");
    Files.setPosixFilePermissions(this.script, all);
  }

  private void installPackages() {
    try {
      this.installPackagesWithoutRoot();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void installPackagesWithoutRoot() throws IOException {
    final ProcessBuilder builder = new ProcessBuilder(this.script.toAbsolutePath().toString());
    ProcessUtils.captureOutput(builder, this.neon::logConsole);
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
