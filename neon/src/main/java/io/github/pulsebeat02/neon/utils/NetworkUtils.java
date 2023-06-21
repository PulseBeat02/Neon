package io.github.pulsebeat02.neon.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class NetworkUtils {

  private NetworkUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static void downloadFile(@NotNull final String url, @NotNull final Path dest)
      throws IOException {
    final URL website = new URL(url);
    final ReadableByteChannel rbc = Channels.newChannel(website.openStream());
    try (final FileOutputStream fos = new FileOutputStream(dest.toFile())) {
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }
  }
}
