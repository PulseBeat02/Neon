package io.github.pulsebeat02.neon.apt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.json.GsonProvider;
import io.github.pulsebeat02.neon.utils.NetworkUtils;
import io.github.pulsebeat02.neon.utils.ProcessUtils;
import io.github.pulsebeat02.neon.utils.ResourceUtils;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class PackageManager {

  private static @NotNull final HttpClient HTTP_CLIENT;
  private static @NotNull final String CPU_ARCH;

  static {
    HTTP_CLIENT = HttpClient.newBuilder().build();
    final String arch = System.getProperty("os.arch");
    CPU_ARCH = arch.equals("x86_64") ? "amd64" : arch.equals("aarch64") ? "arm64" : arch;
  }

  private @NotNull final Neon neon;
  private @NotNull final Path dest;
  private @NotNull final Set<String> repos;
  private @NotNull final Set<AptPackage> packages;

  public PackageManager(@NotNull final Neon neon) {
    this.neon = neon;
    this.dest = neon.getDataFolder().toPath().resolve("packages");
    this.repos = this.getRepos();
    this.packages = this.getPackages();
  }

  public void installPackages() throws IOException {
    if (this.isUnix()) {
      this.createFolders();
      this.downloadPackages();
      this.buildPackages();
      this.loadLibraries();
    }
  }

  private boolean isUnix() {
    final String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return OS.contains("nux");
  }

  private void loadLibraries() throws IOException {
    final Path libs = this.dest.resolve("usr").resolve("lib");
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

  private void createFolders() throws IOException {
    if (!Files.isDirectory(this.dest)) {
      Files.createDirectories(this.dest);
    }
  }

  private void buildPackages() {
    try (final Stream<Path> debs = Files.list(this.dest)) {
      debs.parallel().forEach(this::executeDkpgCommand);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void executeDkpgCommand(@NotNull final Path path) {
    try {
      final Path absolute = path.toAbsolutePath();
      final String name = absolute.toString();
      this.executeCommand(name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void executeCommand(@NotNull final String path) throws IOException {
    final ProcessBuilder builder = new ProcessBuilder("dkpg", "-x", path, ".");
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
