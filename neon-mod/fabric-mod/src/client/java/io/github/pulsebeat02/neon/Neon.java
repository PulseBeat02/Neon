package io.github.pulsebeat02.neon;

import io.github.pulsebeat02.neon.events.ServerJoinHandler;
import io.github.pulsebeat02.neon.listener.PluginMessagingResponse;
import net.fabricmc.api.ClientModInitializer;

public final class Neon implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    new ServerJoinHandler();
    new PluginMessagingResponse();
  }
}
