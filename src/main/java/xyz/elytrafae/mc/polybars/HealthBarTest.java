package xyz.elytrafae.mc.polybars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.api.HealthStylePolyBar;

public class HealthBarTest extends HealthStylePolyBar {
    public HealthBarTest(Identifier id, Identifier fullTexture, Identifier halfTexture, Identifier emptyTexture, int priority) {
        super(id, fullTexture, halfTexture, emptyTexture, 1, priority);
    }

    public HealthBarTest(Identifier id, Identifier fullTexture, Identifier halfTexture, Identifier emptyTexture) {
        this(id, fullTexture, halfTexture, emptyTexture, 0);
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        return player.getMaxHealth();
    }

    @Override
    public double getValue(ServerPlayer player) {
        return player.getHealth();
    }
}
