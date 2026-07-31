package xyz.elytrafae.mc.polybars.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.elytrafae.mc.polybars.multiplexer.ActionBarMultiplexer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMountMixin {

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("RETURN"))
    private void onStartRiding(Entity vehicle, boolean force, boolean thirdParam, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            ServerPlayer player = (ServerPlayer) (Object) this;
            Component mountMessage = Component.translatable("mount.onboard", Component.keybind("key.sneak"));
            ActionBarMultiplexer.getInstance().cacheVanillaActionBar(player, mountMessage);
        }
    }
}
