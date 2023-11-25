package io.github.pulsebeat02.neon.packet;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;

public final class NeonFrameUpdateS2CPacket implements Serializable {
  @Serial private static final long serialVersionUID = 2863313598102499399L;
  public final NeonMapPacket[] frames;

  public NeonFrameUpdateS2CPacket(final NeonMapPacket[] frames) {
    this.frames = frames;
  }

  public byte @NotNull [] serialize() {
    return squish(SerializationUtils.serialize(this));
  }

  public static @NotNull NeonFrameUpdateS2CPacket deserialize(final byte @NotNull [] data) {
    final NeonFrameUpdateS2CPacket packet = SerializationUtils.deserialize(expand(data));
    for (final NeonMapPacket frame : packet.frames) {
      frame.decodeMapData();
    }
    return packet;
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
}
