package io.github.pulsebeat02.neon.events;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;

public final class ServerJoinHandler {
  private @NotNull final SimpleChannel handshake;

  public ServerJoinHandler() {
    this.handshake =
        ChannelBuilder.named("neon:HANDHSHAKE")
            .optional()
            .networkProtocolVersion(1)
            .simpleChannel();
  }

  @SubscribeEvent
  public void onEntityLevelJoin(@NotNull final EntityJoinLevelEvent event) {
    final Entity entity = event.getEntity();
    if (!(entity instanceof final Player player)) {
      return;
    }
    final UUID uuid = player.getUUID();
    final MinecraftServer server = player.getServer();
    final ServerPlayer serverPlayer = server.getPlayerList().getPlayer(uuid);
    this.handshake.send(uuid.toString(), PacketDistributor.PLAYER.with(serverPlayer));
  }
}
