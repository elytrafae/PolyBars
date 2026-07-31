package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.api.HealthStylePolyBar;

public class DefaultHealthBar extends HealthStylePolyBar {
    public DefaultHealthBar(Identifier id, Identifier fullTexture, Identifier halfTexture, Identifier emptyTexture, int sliceCount, int priority) {
        super(id, fullTexture, halfTexture, emptyTexture, sliceCount, priority);
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
