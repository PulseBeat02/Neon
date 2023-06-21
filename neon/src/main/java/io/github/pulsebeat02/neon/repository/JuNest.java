package io.github.pulsebeat02.neon.repository;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.utils.NetworkUtils;
import io.github.pulsebeat02.neon.utils.ProcessUtils;
import io.github.pulsebeat02.neon.utils.ZipUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class JuNest {

  private static @NotNull final String JUNEST_URL;
  private static final Set<String> JCEF_DEPENDENCIES;

  static {
    JUNEST_URL = "https://github.com/fsquillace/junest/archive/refs/heads/master.zip";
    JCEF_DEPENDENCIES = Set.of("libxtst6", "libxi6", "libnss3");
  }

  private @NotNull final Neon neon;
  private @NotNull final Path folder;
  private @NotNull final Path zip;
  private @NotNull final Path script;

  public JuNest(@NotNull final Neon neon) throws IOException {
    this.neon = neon;
    this.folder = neon.getDataFolder().toPath().resolve("junest");
    this.zip = this.folder.resolve("master.zip");
    this.script = this.folder.resolve("junest-master/bin/junest");
    if (this.isUnix()) {
      this.createFolders();
      this.checkInstallation();
    }
  }

  private boolean isUnix() {
    final String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return OS.contains("nux");
  }

  private void checkInstallation() throws IOException {
    if (!Files.exists(this.script)) {
      this.downloadJuNest();
      this.extractJuNest();
      this.setExecutePermissions();
      this.setupJuNest();
    }
  }

  private void installPackage(@NotNull final String pkg) throws IOException {
    final String absolute = this.script.toAbsolutePath().toString();
    final ProcessBuilder builder = new ProcessBuilder(absolute, "install", pkg);
    ProcessUtils.captureOutput(builder, this.neon::logConsole);
  }

  private void installPackages() {
    // JCEF_DEPENDENCIES.forEach();
  }

  private void installPackage() throws IOException {
    this.executeJuNestCommand();
  }

  private void executeJuNestCommand() throws IOException {
    final String absolute = this.script.toAbsolutePath().toString();
    final ProcessBuilder builder = new ProcessBuilder(absolute, "proot", "-f");
    ProcessUtils.captureOutput(builder, this.neon::logConsole);
  }

  private void setupJuNest() throws IOException {
    final String absolute = this.script.toAbsolutePath().toString();
    final ProcessBuilder builder = new ProcessBuilder(absolute, "setup");
    ProcessUtils.captureOutput(builder, this.neon::logConsole);
  }

  private void createFolders() throws IOException {
    if (!Files.isDirectory(this.folder)) {
      Files.createDirectories(this.folder);
    }
  }

  private void downloadJuNest() throws IOException {
    NetworkUtils.downloadFile(JUNEST_URL, this.zip);
  }

  private void extractJuNest() throws IOException {
    ZipUtils.unzip(this.zip, this.folder);
    Files.deleteIfExists(this.zip);
  }

  private void setExecutePermissions() throws IOException {
    final Set<PosixFilePermission> all = PosixFilePermissions.fromString("rwxrwxrwx");
    final Set<String> files =
        Set.of(
            "junest-master/lib/utils/utils.sh",
            "junest-master/lib/core/common.sh",
            "junest-master/lib/core/build.sh",
            "junest-master/lib/core/setup.sh",
            "junest-master/lib/core/chroot.sh",
            "junest-master/lib/core/namespace.sh",
            "junest-master/lib/core/proot.sh",
            "junest-master/lib/core/wrappers.sh");
    files.forEach((file) -> this.setFilePermission(file, all));
    Files.setPosixFilePermissions(this.script, all);
  }

  private void setFilePermission(
      @NotNull final String relativePath, @NotNull final Set<PosixFilePermission> all) {
    final Path absolute = this.folder.resolve(relativePath);
    try {
      Files.setPosixFilePermissions(absolute, all);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
