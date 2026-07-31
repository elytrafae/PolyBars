package xyz.elytrafae.mc.polybars.mixin;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.elytrafae.mc.polybars.multiplexer.ActionBarMultiplexer;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {

    @Inject(
        method = {
            "send(Lnet/minecraft/network/protocol/Packet;)V",
            "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V"
        },
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (ActionBarMultiplexer.getInstance().isSendingInternal()) {
            return;
        }

        if ((Object) this instanceof ServerGamePacketListenerImpl gameListener) {
            ServerPlayer player = gameListener.player;
            if (player == null) return;

            if (handleActionBarPacket(player, packet)) {
                ci.cancel();
            }
        }
    }

    private boolean handleActionBarPacket(ServerPlayer player, Packet<?> packet) {
        if (packet instanceof ClientboundSetActionBarTextPacket actionBarPacket) {
            Component text = actionBarPacket.text();
            ActionBarMultiplexer.getInstance().cacheVanillaActionBar(player, text);
            return true;
        } else if (packet instanceof ClientboundSystemChatPacket systemChatPacket) {
            if (systemChatPacket.overlay()) {
                Component text = systemChatPacket.content();
                ActionBarMultiplexer.getInstance().cacheVanillaActionBar(player, text);
                return true;
            }
        } else if (packet instanceof ClientboundBundlePacket bundlePacket) {
            boolean handledAny = false;
            for (Packet<?> subPacket : bundlePacket.subPackets()) {
                if (handleActionBarPacket(player, subPacket)) {
                    handledAny = true;
                }
            }
            return handledAny;
        }
        return false;
    }
}
