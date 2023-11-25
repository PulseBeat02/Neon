import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

public class CompressionTest {

  public static void main(final String[] args) {
    final String salt = getSaltString();
    System.out.println(salt);
    final String compressed = new String(squish(salt.getBytes()));
    System.out.println(compressed);
    final String decompressed = new String(expand(compressed.getBytes()));
    System.out.println(decompressed);
    System.out.println(salt.equals(decompressed));
  }

  private static @NotNull String getSaltString() {
    final String SALTCHARS = "0123456789";
    final StringBuilder salt = new StringBuilder();
    final Random rnd = new Random();
    while (salt.length() < 100000) {
      final int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }
    return salt.toString();
  }

  private static byte @NotNull [] squish(final byte @NotNull [] bloated) {
    try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte lastByte = bloated[0];
      int matchCount = 1;
      for (int i = 1; i < bloated.length; i++) {
        final byte thisByte = bloated[i];
        if (lastByte == thisByte) {
          matchCount++;
        } else {
          out.write((byte) matchCount);
          out.write(lastByte);
          matchCount = 1;
          lastByte = thisByte;
        }
      }
      out.write((byte) matchCount);
      out.write(lastByte);
      return out.toByteArray();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static byte @NotNull [] expand(final byte @NotNull [] squishedMapData) {
    try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      for (int i = 0; i < squishedMapData.length; i += 2) {
        final int count = squishedMapData[i];
        final byte value = squishedMapData[i + 1];
        for (int j = 0; j < count; j++) {
          out.write(value);
        }
      }
      return out.toByteArray();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
