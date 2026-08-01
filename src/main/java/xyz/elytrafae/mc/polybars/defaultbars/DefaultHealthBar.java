package xyz.elytrafae.mc.polybars.defaultbars;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mutable;
import xyz.elytrafae.mc.polybars.api.AbstractPolyBar;
import xyz.elytrafae.mc.polybars.api.HealthStylePolyBar;
import xyz.elytrafae.mc.polybars.api.PolyBarTexture;
import xyz.elytrafae.mc.polybars.api.PolyTextureSliceMode;
import xyz.elytrafae.mc.polybars.font.ComponentWidthCalculator;
import xyz.elytrafae.mc.polybars.font.SpaceBuilder;

import java.io.IOException;
import java.util.List;

public class DefaultHealthBar extends AbstractPolyBar {

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
                new PolyBarTexture(baseTextureId.withSuffix("below_fill"), 1, PolyTextureSliceMode.INDIVIDUAL)
        ), priority);
    }

    public double getMaxValue(ServerPlayer player) {
        return player.getMaxHealth();
    }

    public double getValue(ServerPlayer player) {
        return player.getHealth();
    }
    public int getIconSpacing() {
        return -2;
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return true;
    }

    @Override
    public Component getBarComponent(ServerPlayer player) {
        int maxValue = (int)Math.ceil(getMaxValue(player));
        int currValue = (int)Math.ceil(getValue(player));

        int iconCount = Math.min((int)Math.ceil(maxValue/2.0), 10);
        int sliceIndex = (player.hurtTime % 4 <= 1 ? 0 : 2) + (player.level().getServer().isHardcore() ? 1 : 0);

        MutableComponent bar = Component.empty();
        MutableComponent containers = getBarLayer(iconCount*2, 0, 0, sliceIndex, iconCount);
        MutableComponent resetti = SpaceBuilder.getSpaceComponent(-ComponentWidthCalculator.calculateWidth(containers, false));

        bar.append(containers);
        bar.append(resetti);

        if (currValue > iconCount*2) {
            MutableComponent belowBar = getBarLayer(iconCount*2, 9, 9, 0, iconCount).withColor(0x880000);
            bar.append(belowBar);
            bar.append(resetti.copy());
            currValue %= (iconCount * 2);
            if (currValue == 0) {
                currValue = iconCount*2;
            }
        }

        int halfTextureIndex;
        if (player.hasEffect(MobEffects.WITHER)) {
            halfTextureIndex = 7;
        } else if (player.hasEffect(MobEffects.POISON)) {
            halfTextureIndex = 5;
        } else if (player.isFreezing()) {
            halfTextureIndex = 3;
        } else {
            halfTextureIndex = 1;
        }

        bar.append(getBarLayer(currValue, halfTextureIndex+1, halfTextureIndex, sliceIndex, iconCount));
        return bar;
    }

    private MutableComponent getBarLayer(int value, int fullTextureIndex, int halfTextureIndex, int sliceIndex, int iconCount) {
        int i;
        MutableComponent layer = Component.empty();
        for (i=0; i < (value/2*2); i+=2) {
            if (i > 0) {
                layer.append(SpaceBuilder.getSpaceComponent(getIconSpacing()));
            }
            layer.append(getSliceComponent(fullTextureIndex, sliceIndex));
        }
        if (i < value) {
            if (i > 0) {
                layer.append(SpaceBuilder.getSpaceComponent(getIconSpacing()));
            }
            layer.append(getSliceComponent(halfTextureIndex, sliceIndex));
            i+=2;
        }
        layer.append(SpaceBuilder.getSpaceComponent(10 * (iconCount - (i/2))));
        return layer;
    }

}
