package io.github.pulsebeat02.neon.listener;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

public final class MessagingHandler implements ClientPlayNetworking.PlayChannelHandler {

  @Override
  public void receive(
      @NotNull final MinecraftClient client,
      @NotNull final ClientPlayNetworkHandler handler,
      @NotNull final PacketByteBuf buf,
      @NotNull final PacketSender responseSender) {

  }
}
