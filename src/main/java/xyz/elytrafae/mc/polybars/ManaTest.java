package xyz.elytrafae.mc.polybars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.api.ExperienceStylePolyBar;

public class ManaTest extends ExperienceStylePolyBar {
    public ManaTest(Identifier id, Identifier bgTexture, Identifier fillTexture, int fillSlicesCount, int priority) {
        super(id, bgTexture, fillTexture, fillSlicesCount, priority);
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        return 20;
    }

    @Override
    public double getValue(ServerPlayer player) {
        return player.getFoodData().getSaturationLevel();
    }
}
