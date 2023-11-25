package io.github.pulsebeat02.neon.listener;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public final class PluginMessagingResponse {

  private static final Identifier MESSAGE_ID;

  static {
    MESSAGE_ID = new Identifier("neon", "map");
  }

  public PluginMessagingResponse() {
    this.registerListener();
  }

  private void registerListener() {
    ClientPlayNetworking.registerGlobalReceiver(MESSAGE_ID, new MessagingHandler());
  }
}
