package io.github.pulsebeat02.neon.packet;

import java.io.Serial;
import java.io.Serializable;

public final class NeonMapPacket implements Serializable {

  @Serial private static final long serialVersionUID = 4147643121501630471L;
  public final int id;
  public final int centerX;
  public final int centerZ;
  public byte[] mapData;

  public NeonMapPacket(final int id, final int centerX, final int centerZ, final byte[] mapData) {
    this.id = id;
    this.centerX = centerX;
    this.centerZ = centerZ;
    this.mapData = ColorPacketEncoder.encode(mapData);
  }

  public void decodeMapData() {
    this.mapData = ColorPacketEncoder.decode(this.mapData);
  }
}
