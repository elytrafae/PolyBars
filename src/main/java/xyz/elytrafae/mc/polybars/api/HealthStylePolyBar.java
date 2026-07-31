package xyz.elytrafae.mc.polybars.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.font.SpaceBuilder;

import java.util.List;

/**
 * Reusable abstract bar class representing a health-style HUD bar composed of 10 heart/icon containers.
 * Requires three 1-slice textures for full, half, and empty states.
 */
public abstract class HealthStylePolyBar extends AbstractPolyBar {

    public static final int ICON_COUNT = 10; // 10 whole icons (20 halves)

    public HealthStylePolyBar(Identifier id, Identifier fullTexture, Identifier halfTexture, Identifier emptyTexture, int priority) {
        super(id, List.of(
                new PolyBarTexture(fullTexture, 1, PolyTextureSliceMode.INDIVIDUAL),  // Texture index 0: Full
                new PolyBarTexture(halfTexture, 1, PolyTextureSliceMode.INDIVIDUAL),  // Texture index 1: Half
                new PolyBarTexture(emptyTexture, 1, PolyTextureSliceMode.INDIVIDUAL)  // Texture index 2: Empty
        ), priority);
    }

    public HealthStylePolyBar(Identifier id, Identifier fullTexture, Identifier halfTexture, Identifier emptyTexture) {
        this(id, fullTexture, halfTexture, emptyTexture, 0);
    }

    /**
     * Maximum value of the bar (e.g., 20.0 for 10 hearts).
     */
    public abstract double getMaxValue(ServerPlayer player);

    /**
     * Current value of the bar (e.g., 15.0 for 7.5 hearts).
     */
    public abstract double getValue(ServerPlayer player);

    /**
     * Text/glyph tint color in RGB format. Defaults to white (0xFFFFFF).
     */
    public int getColor(ServerPlayer player) {
        return 0xFFFFFF;
    }

    /**
     * Pixel spacing offset between icon containers in the bar.
     * Defaults to -2 pixels (overlapping/tight icon spacing).
     * NOTE: By default, Minecraft spaces icons one pixel apart!
     *
     * @param player Target player
     * @return Pixel spacing value (-2 shifts icons 2px closer together, so they overlap by 1px)
     */
    public int getIconSpacing(ServerPlayer player) {
        return -2;
    }

    /**
     * The slice index of all icons shown. Useful for varians
     * (ex. regular hunger vs under the effects of the Hunger effect).
     * Defaults to 0
     *
     * @param player Target player
     * @return The icon slice index
     */
    public int getShownIconSliceIndex(ServerPlayer player) {
        return 0;
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return true;
    }

    @Override
    public Component getBarComponent(ServerPlayer player) {
        double max = getMaxValue(player);
        double val = getValue(player);

        int adjusted_val = (int)Math.ceil(val/max) * (ICON_COUNT *2);
        int iconIndex = getShownIconSliceIndex(player);

        MutableComponent barComp = Component.empty();

        int spacing = getIconSpacing(player);

        for (int i = 0; i < ICON_COUNT; i++) {
            if (i > 0 && spacing != 0) {
                if (spacing < 0) {
                    barComp.append(SpaceBuilder.getNegativeSpaceComponent(Math.abs(spacing)));
                } else {
                    barComp.append(SpaceBuilder.getPositiveSpaceComponent(spacing));
                }
            }

            double containerStart = i * 2.0;
            double remaining = adjusted_val - containerStart;

            if (remaining >= 2.0) {
                barComp.append(getSliceComponent(0, iconIndex));
            } else if (remaining >= 1.0) {
                barComp.append(getSliceComponent(1, iconIndex));
            } else {
                barComp.append(getSliceComponent(2, iconIndex));
            }
        }

        int color = getColor(player);
        barComp.withStyle(style -> style.withoutShadow().withColor(color));

        return barComp;
    }
}
