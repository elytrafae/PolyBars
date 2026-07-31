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

    public static final int DEFAULT_ICON_COUNT = 10;

    public HealthStylePolyBar(Identifier id, Identifier fullTexture, Identifier halfTexture, Identifier emptyTexture, int sliceCount, int priority) {
        super(id, List.of(
                new PolyBarTexture(fullTexture, sliceCount, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(halfTexture, sliceCount, PolyTextureSliceMode.INDIVIDUAL),
                new PolyBarTexture(emptyTexture, sliceCount, PolyTextureSliceMode.INDIVIDUAL)
        ), priority);
    }

    /**
     * Gets the number of drawn icon containers for this bar.
     * Defaults to 10.
     *
     * @param player Target player
     * @return Number of icon containers (clamped between 1 and 20)
     */
    public int getIconCount(ServerPlayer player) {
        return DEFAULT_ICON_COUNT;
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
     * The slice index of all icons shown. Useful for variants
     * (ex. regular hunger vs under the effects of the Hunger effect).
     * Defaults to 0
     *
     * @param player Target player
     * @return The icon slice index
     */
    public int getShownIconSliceIndex(ServerPlayer player) {
        return 0;
    }

    /**
     * Controls the icons' ordering.
     * If false, draws from left to right (like health).
     * If true, draws from right to left (like hunger).
     * Returns false by default.
     *
     * @param player Target player
     * @return The boolean described above.
     */
    public boolean isOrderReversed(ServerPlayer player) {
        return false;
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return true;
    }

    @Override
    public Component getBarComponent(ServerPlayer player) {
        double max = getMaxValue(player);
        double val = getValue(player);

        int totalIcons = getIconCount(player);
        double ratio = max > 0 ? Math.clamp(val / max, 0.0, 1.0) : 0.0;
        int adjusted_val = (int) Math.ceil(ratio * (totalIcons * 2));
        int iconIndex = getShownIconSliceIndex(player);

        int spacing = getIconSpacing(player);
        MutableComponent[] sliceComponents = new MutableComponent[totalIcons];

        for (int i = 0; i < totalIcons; i++) {
            double containerStart = i * 2.0;
            double remaining = adjusted_val - containerStart;

            if (remaining >= 2.0) {
                sliceComponents[i] = getSliceComponent(0, iconIndex);
            } else if (remaining >= 1.0) {
                sliceComponents[i] = getSliceComponent(1, iconIndex);
            } else {
                sliceComponents[i] = getSliceComponent(2, iconIndex);
            }
        }

        if (isOrderReversed(player)) {
            for(int i = 0; i < sliceComponents.length / 2; i++) {
                MutableComponent temp = sliceComponents[i];
                sliceComponents[i] = sliceComponents[sliceComponents.length - i - 1];
                sliceComponents[sliceComponents.length - i - 1] = temp;
            }
        }

        MutableComponent barComp = Component.empty();
        for (int i=0; i < sliceComponents.length; i++) {
            if (i > 0 && spacing != 0) {
                barComp.append(SpaceBuilder.getSpaceComponent(spacing));
            }
            barComp.append(sliceComponents[i]);
        }

        int color = getColor(player);
        barComp.withStyle(style -> style.withoutShadow().withColor(color));

        return barComp;
    }
}
