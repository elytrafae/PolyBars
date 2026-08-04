package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.HealthBarTest;
import xyz.elytrafae.mc.polybars.api.HealthStylePolyBar;

public class DefaultAirBar extends HealthStylePolyBar {
    public DefaultAirBar(Identifier id, Identifier baseTextureId, int priority) {
        super(id, baseTextureId, baseTextureId.withSuffix("_bursting"), baseTextureId.withSuffix("_empty"), 1, priority);
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        return player.getMaxAirSupply();
    }

    @Override
    public double getValue(ServerPlayer player) {
        return player.getAirSupply();
    }

    @Override
    public boolean isOrderReversed(ServerPlayer player) {
        return true;
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return player.getAirSupply() < player.getMaxAirSupply() && super.shouldDraw(player);
    }

}
