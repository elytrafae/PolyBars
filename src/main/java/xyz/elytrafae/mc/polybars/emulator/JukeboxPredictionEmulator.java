package xyz.elytrafae.mc.polybars.emulator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.elytrafae.mc.polybars.multiplexer.ActionBarMultiplexer;

import java.util.Optional;

public class JukeboxPredictionEmulator {

    public static boolean handleJukeboxInteraction(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack heldItem) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof JukeboxBlock)) {
            return false;
        }

        Optional<Holder<JukeboxSong>> optionalSong = JukeboxSong.fromStack(heldItem);
        if (optionalSong.isEmpty()) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof JukeboxBlockEntity jukeboxBE) {
            if (jukeboxBE.getItem(0).isEmpty()) {
                Component songDescription = optionalSong.get().value().description();
                Component nowPlaying = Component.translatable("record.nowPlaying", songDescription);
                ActionBarMultiplexer.getInstance().cacheVanillaActionBar(player, nowPlaying);
                return true;
            }
        }

        return false;
    }
}
