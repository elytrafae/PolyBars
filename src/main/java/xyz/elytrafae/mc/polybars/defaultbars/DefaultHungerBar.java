package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.api.HealthStylePolyBar;

public class DefaultHungerBar extends HealthStylePolyBar {
    public DefaultHungerBar(Identifier id, Identifier baseTextureId, int priority) {
        super(id, baseTextureId.withSuffix("_full"), baseTextureId.withSuffix("_half"), baseTextureId.withSuffix("_empty"), priority);
    }

    public DefaultHungerBar(Identifier id, Identifier baseTextureId) {
        this(id, baseTextureId, 0);
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        return 20;
    }

    @Override
    public double getValue(ServerPlayer player) {
        return player.getFoodData().getFoodLevel();
    }
}
