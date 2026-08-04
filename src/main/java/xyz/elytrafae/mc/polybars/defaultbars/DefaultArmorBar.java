package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import xyz.elytrafae.mc.polybars.api.HealthStylePolyBar;

public class DefaultArmorBar extends HealthStylePolyBar {
    public DefaultArmorBar(Identifier id, Identifier baseTextureId, int priority) {
        super(id, baseTextureId.withSuffix("_full"), baseTextureId.withSuffix("_half"), baseTextureId.withSuffix("_empty"), 1, priority);
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        if (!(Attributes.ARMOR.value() instanceof RangedAttribute)) {
            return 30;
        }
        return ((RangedAttribute)Attributes.ARMOR.value()).getMaxValue();
    }

    @Override
    public double getValue(ServerPlayer player) {
        return player.getArmorValue();
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return player.getArmorValue() > 0 && super.shouldDraw(player);
    }
}
