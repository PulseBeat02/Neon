package io.github.pulsebeat02.neon.listener;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public final class PluginMessagingResponse {

  private static final Identifier MAP_DATA_MESSAGE_ID;

  static {
    MAP_DATA_MESSAGE_ID = new Identifier("neon", "map_data");
  }

  public PluginMessagingResponse() {
    this.registerListener();
  }

  private void registerListener() {
    ClientPlayNetworking.registerGlobalReceiver(MAP_DATA_MESSAGE_ID, new MapDataMessagingHandler());
  }
}
