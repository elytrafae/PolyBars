package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import xyz.elytrafae.mc.polybars.api.LayeredHealthStylePolyBar;
import xyz.elytrafae.mc.polybars.api.PolyBarTexture;
import xyz.elytrafae.mc.polybars.api.PolyTextureSliceMode;

import java.util.List;

public class DefaultVehicleHealthBar extends LayeredHealthStylePolyBar {

    public DefaultVehicleHealthBar(Identifier id, Identifier baseTextureId, int priority) {
        super(id, List.of(
                new PolyBarTexture(baseTextureId.withSuffix("_container"), 1, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("_half"), 1, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(baseTextureId.withSuffix("_full"), 1, PolyTextureSliceMode.INDIVIDUAL)
        ), priority, 1);
    }


    @Override
    public int getIconCount(ServerPlayer player) {
        return (int) Math.clamp(Math.ceil(getMaxValue(player, 0) / 2), 1, 10);
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return player.getVehicle() instanceof LivingEntity living && living.isAlive();
    }

    @Override
    public boolean isOrderReversed(ServerPlayer player) {
        return true;
    }

    @Override
    public double getValue(ServerPlayer player, int layer) {
        return player.getVehicle() instanceof LivingEntity mount ? mount.getHealth() : 0;
    }

    @Override
    public double getMaxValue(ServerPlayer player, int layer) {
        return player.getVehicle() instanceof LivingEntity mount ? mount.getMaxHealth() : 20;
    }

    @Override
    public int getFullTextureIndex(ServerPlayer player, int layer) {
        return 2;
    }

    @Override
    public int getHalfTextureIndex(ServerPlayer player, int layer) {
        return 1;
    }
}
