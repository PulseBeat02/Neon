package io.github.pulsebeat02.neon;

import io.github.pulsebeat02.neon.events.ServerJoinHandler;
import io.github.pulsebeat02.neon.listener.MapDataMessagingHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(Neon.MOD_ID)
public final class Neon {

  public static final String MOD_ID = "neon";

  public Neon() {
    MinecraftForge.EVENT_BUS.register(new ServerJoinHandler());
    new MapDataMessagingHandler();
  }
}
