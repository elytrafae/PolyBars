package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import xyz.elytrafae.mc.polybars.api.LayeredHealthStylePolyBar;
import xyz.elytrafae.mc.polybars.api.PolyBarTexture;
import xyz.elytrafae.mc.polybars.api.PolyTextureSliceMode;

import java.util.List;

public class DefaultHealthBar extends LayeredHealthStylePolyBar {

    public DefaultHealthBar(Identifier id, Identifier baseTextureId, int priority) {
        super(id, List.of(
                new PolyBarTexture(baseTextureId.withSuffix("container"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("half"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("full"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("frozen_half"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("frozen_full"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("poisoned_half"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("poisoned_full"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("withered_half"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("withered_full"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("absorbing_half"), 4, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("absorbing_full"), 4, PolyTextureSliceMode.INDIVIDUAL)
        ), priority, 2);
    }

    @Override
    public int getIconCount(ServerPlayer player) {
        return (int) Math.clamp(Math.ceil(getMaxValue(player, 0) / 2), 1, 10);
    }

    @Override
    public int getSliceIndex(ServerPlayer player) {
        return (player.hurtTime % 4 <= 1 ? 0 : 2) + (player.level().getServer().isHardcore() ? 1 : 0);
    }

    @Override
    public double getValue(ServerPlayer player, int layer) {
        return layer == 0 ? player.getHealth() : player.getAbsorptionAmount();
    }

    @Override
    public double getMaxValue(ServerPlayer player, int layer) {
        return layer == 0 ? player.getMaxHealth() : player.getMaxAbsorption();
    }

    @Override
    public int getFullTextureIndex(ServerPlayer player, int layer) {
        if (layer == 1) return 10;
        if (player.hasEffect(MobEffects.WITHER)) return 8;
        if (player.hasEffect(MobEffects.POISON)) return 6;
        if (player.isFreezing()) return 4;
        return 2;
    }

    @Override
    public int getHalfTextureIndex(ServerPlayer player, int layer) {
        if (layer == 1) return 9;
        if (player.hasEffect(MobEffects.WITHER)) return 7;
        if (player.hasEffect(MobEffects.POISON)) return 5;
        if (player.isFreezing()) return 3;
        return 1;
    }
}
