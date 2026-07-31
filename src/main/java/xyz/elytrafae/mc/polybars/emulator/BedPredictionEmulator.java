package xyz.elytrafae.mc.polybars.emulator;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import xyz.elytrafae.mc.polybars.multiplexer.ActionBarMultiplexer;

public class BedPredictionEmulator {

    public static boolean handleBedInteraction(ServerPlayer player, ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return false;
        }

        Either<Player.BedSleepingProblem, Unit> result = player.startSleepInBed(pos);
        if (result.left().isPresent()) {
            Player.BedSleepingProblem problem = result.left().get();
            Component message = problem.message();
            if (message != null) {
                ActionBarMultiplexer.getInstance().cacheVanillaActionBar(player, message);
                return true;
            }
        }

        return false;
    }
}
