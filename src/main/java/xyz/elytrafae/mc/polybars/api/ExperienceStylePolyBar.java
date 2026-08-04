package xyz.elytrafae.mc.polybars.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Reusable abstract bar class representing a classic XP-style continuous HUD bar.
 * Requires two textures: a background frame (1 slice) and a fill texture with S slices.
 */
public abstract class ExperienceStylePolyBar extends AbstractPolyBar {

    private final int fillSlicesCount;

    public ExperienceStylePolyBar(Identifier id, Identifier bgTexture, Identifier fillTexture, int fillSlicesCount, int priority) {
        super(id, List.of(
                new PolyBarTexture(bgTexture, 1, PolyTextureSliceMode.INCREMENTAL),
                new PolyBarTexture(fillTexture, fillSlicesCount, PolyTextureSliceMode.INCREMENTAL)
        ), priority);
        this.fillSlicesCount = fillSlicesCount;
    }

    public ExperienceStylePolyBar(Identifier id, Identifier bgTexture, Identifier fillTexture, int fillSlicesCount) {
        this(id, bgTexture, fillTexture, fillSlicesCount, 0);
    }

    /**
     * Maximum value of the bar.
     */
    public abstract double getMaxValue(ServerPlayer player);

    /**
     * Current value of the bar.
     */
    public abstract double getValue(ServerPlayer player);

    /**
     * Text/glyph tint color in RGB format. Defaults to white (0xFFFFFF).
     */
    public int getColor(ServerPlayer player) {
        return 0xFFFFFF;
    }

    public int getFillSlicesCount() {
        return fillSlicesCount;
    }

    @Override
    public Component getBarComponent(ServerPlayer player) {
        double max = getMaxValue(player);
        double val = getValue(player);

        double ratio = max > 0 ? Math.min(1.0, Math.max(0.0, val / max)) : 0.0;
        int sliceIndex = (int) Math.round(ratio * (fillSlicesCount - 1));

        // Assembles the fill slice glyph corresponding to current progress
        MutableComponent barComp = getSliceComponent(1, sliceIndex);

        int color = getColor(player);
        barComp.withStyle(style -> {
            var updated = style.withoutShadow();
            if (color != 0xFFFFFF) {
                updated = updated.withColor(color);
            }
            return updated;
        });

        return barComp;
    }
}
