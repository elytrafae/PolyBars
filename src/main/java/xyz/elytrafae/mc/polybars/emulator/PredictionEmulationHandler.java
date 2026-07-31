package xyz.elytrafae.mc.polybars.emulator;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public class PredictionEmulationHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel serverLevel) {
                BlockPos pos = hitResult.getBlockPos();
                ItemStack heldItem = player.getItemInHand(hand);

                if (BedPredictionEmulator.handleBedInteraction(serverPlayer, serverLevel, pos)) {
                    return InteractionResult.PASS;
                }

                if (JukeboxPredictionEmulator.handleJukeboxInteraction(serverPlayer, serverLevel, pos, heldItem)) {
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
