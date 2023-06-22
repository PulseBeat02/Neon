package io.github.pulsebeat02.neon.apt;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.json.GsonProvider;
import io.github.pulsebeat02.neon.utils.NetworkUtils;
import io.github.pulsebeat02.neon.utils.ProcessUtils;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Stream;

import io.github.pulsebeat02.neon.utils.unsafe.UnsafeUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class PackageManager {

  private static @NotNull final HttpClient HTTP_CLIENT;
  private static @NotNull final String ORIGINAL_CPU_ARCH;
  private static @NotNull final String CPU_ARCH;

  static {
    HTTP_CLIENT = HttpClient.newBuilder().build();
    ORIGINAL_CPU_ARCH = System.getProperty("os.arch");
    CPU_ARCH =
        ORIGINAL_CPU_ARCH.equals("x86_64")
            ? "amd64"
            : ORIGINAL_CPU_ARCH.equals("aarch64") ? "arm64" : ORIGINAL_CPU_ARCH;
  }

  private @NotNull final Neon neon;
  private @NotNull final Path dest;
  private @NotNull final Path script;
  private @NotNull final Set<String> repos;
  private @NotNull final Set<AptPackage> packages;

  public PackageManager(@NotNull final Neon neon) {
    this.neon = neon;
    this.dest = neon.getDataFolder().toPath().resolve("packages");
    this.script = this.dest.resolve("compile");
    this.repos = this.getRepos();
    this.packages = this.getPackages();
  }

  public void installPackages() throws IOException {
    if (this.isUnix()) {
      this.createFolders();
      this.copyScript();
      this.changeScriptPermissions();
      this.downloadPackages();
      this.buildPackages();
      this.addNativePath();
    }
  }

  private boolean isUnix() {
    final String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return OS.contains("nux");
  }

  private void copyScript() {
    try (final InputStream stream = ResourceUtils.getResourceAsStream("package/compile")) {
      Files.copy(stream, this.script, REPLACE_EXISTING);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void changeScriptPermissions() throws IOException {
    final Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
    Files.setPosixFilePermissions(this.script, permissions);
  }

  private void addNativePath() throws IOException {
    final String pathToAdd = this.resolveNativeLibraryPath().toString();
    final URL url = new URL(pathToAdd);
    final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    final ClassLoader urlCL = URLClassLoader.newInstance(new URL[] {url}, contextCL);
    Thread.currentThread().setContextClassLoader(urlCL);
  }

  private @NotNull Path resolveNativeLibraryPath() throws IOException {
    final Path folder = this.dest.resolve("usr").resolve("lib");
    try (final Stream<Path> files = Files.list(folder)) {
      return this.getPath(files);
    }
  }

  private @NotNull Path getPath(@NotNull final Stream<Path> files) {
    return files
        .filter(file -> file.getFileName().toString().startsWith(ORIGINAL_CPU_ARCH))
        .findFirst()
        .orElseThrow();
  }

  private void createFolders() throws IOException {
    if (!Files.isDirectory(this.dest)) {
      Files.createDirectories(this.dest);
    }
  }

  private void buildPackages() throws IOException {
    final Path absolute = this.script.toAbsolutePath();
    final String cmd = absolute.toString();
    final ProcessBuilder builder = new ProcessBuilder(cmd);
    builder.directory(this.dest.toFile());
    ProcessUtils.captureOutput(builder, this.neon::logConsole);
  }

  private void downloadPackages() throws IOException {
    final Set<String> downloads = this.getDownloadUrls();
    for (final String url : downloads) {
      final String name = ResourceUtils.getFilename(url);
      final Path resolve = this.dest.resolve(name);
      NetworkUtils.downloadFile(url, resolve);
    }
  }

  private @NotNull Set<String> getDownloadUrls() {
    final Set<String> downloadUrlBases = new HashSet<>();
    this.packages.parallelStream()
        .forEach((pkg) -> this.handlePackageDownloadUrl(downloadUrlBases));
    return downloadUrlBases;
  }

  private void handlePackageDownloadUrl(@NotNull final Set<String> downloadUrlBases) {
    for (final AptPackage apt : this.packages) {
      final String append = apt.getAppend();
      final String name = apt.getName();
      final List<String> download = this.getBaseUrls(append);
      final String begin = "%s_".formatted(name);
      final String end = "_%s.deb".formatted(CPU_ARCH);
      for (final String url : download) {
        final Optional<String> fileUrl = this.getFileUrl(url, begin, end);
        if (fileUrl.isPresent()) {
          downloadUrlBases.add(fileUrl.get());
          break;
        }
      }
    }
  }

  private @NotNull Optional<String> getFileUrl(
      @NotNull final String folderUrl, @NotNull final String begin, @NotNull final String end) {
    try {
      final List<String> list = new ArrayList<>();
      final Document doc = Jsoup.parse(new URL(folderUrl).openStream(), "UTF8", folderUrl);
      final Element table = doc.select("table").get(0);
      final Elements rows = table.select("tr");
      for (final Element row : rows) {
        final Elements cols = row.select("td");
        if (cols.isEmpty()) {
          continue;
        }
        final String name = cols.get(1).text();
        if (name.startsWith(begin) && (name.endsWith(end) || name.endsWith("_all.deb"))) {
          final String fileUrl = folderUrl + name;
          list.add(fileUrl);
        }
      }
      Collections.sort(list);
      if (list.size() - 1 < 0) {
        return Optional.empty();
      }
      return Optional.of(list.get(list.size() - 1));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @NotNull List<String> getBaseUrls(@NotNull final String append) {
    final List<String> downloads = new ArrayList<>();
    for (final String base : this.repos) {
      final String url = base + append;
      try {
        final int code = this.getResponse(url);
        if (code == 404) {
          continue;
        }
        downloads.add(url);
      } catch (final IOException | URISyntaxException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return downloads;
  }

  private int getResponse(@NotNull final String url)
      throws URISyntaxException, IOException, InterruptedException {
    final URI uri = new URL(url).toURI();
    final HttpResponse.BodyHandler<Void> handler = HttpResponse.BodyHandlers.discarding();
    final HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
    final HttpResponse<Void> response = HTTP_CLIENT.send(request, handler);
    return response.statusCode();
  }

  private Set<String> getRepos() {
    final Gson gson = GsonProvider.getGson();
    try (final Reader reader = ResourceUtils.getResourceAsReader("package/repositories.json")) {
      final Type type = new TypeToken<Set<String>>() {}.getType();
      return gson.fromJson(reader, type);
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }

  private @NotNull Set<AptPackage> getPackages() {
    final Gson gson = GsonProvider.getGson();
    try (final Reader reader = ResourceUtils.getResourceAsReader("package/packages.json")) {
      final Type type = new TypeToken<Map<String, String>>() {}.getType();
      final Map<String, String> map = gson.fromJson(reader, type);
      final Set<AptPackage> packages = new HashSet<>();
      for (final Map.Entry<String, String> entry : map.entrySet()) {
        final String name = entry.getKey();
        final String append = entry.getValue();
        packages.add(new AptPackage(name, append));
      }
      return packages;
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }
}
