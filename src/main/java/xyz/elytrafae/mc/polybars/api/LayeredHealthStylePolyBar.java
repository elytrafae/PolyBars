package xyz.elytrafae.mc.polybars.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.font.ComponentWidthCalculator;
import xyz.elytrafae.mc.polybars.font.SpaceBuilder;

import java.util.List;

public abstract class LayeredHealthStylePolyBar extends AbstractPolyBar {

    private final int defaultLayerCount;

    public LayeredHealthStylePolyBar(Identifier id, List<PolyBarTexture> textures, int priority, int layerCount) {
        super(id, textures, priority);
        this.defaultLayerCount = layerCount;
    }

    public LayeredHealthStylePolyBar(Identifier id, List<PolyBarTexture> textures, int priority) {
        this(id, textures, priority, 1);
    }

    public int getLayerCount(ServerPlayer player) {
        return defaultLayerCount;
    }

    public abstract double getValue(ServerPlayer player, int layer);

    public abstract double getMaxValue(ServerPlayer player, int layer);

    public abstract int getFullTextureIndex(ServerPlayer player, int layer);

    public abstract int getHalfTextureIndex(ServerPlayer player, int layer);

    public int getContainerTextureIndex(ServerPlayer player, int layer) {
        return layer == 0 ? 0 : -1;
    }

    public double getScale(ServerPlayer player, int layer) {
        return 1.0;
    }

    public boolean isTieredOverflowEnabled(ServerPlayer player, int layer) {
        return true;
    }

    public boolean shouldDrawLayer(ServerPlayer player, int layer) {
        return getValue(player, layer) > 0;
    }

    public boolean isOrderReversed(ServerPlayer player) {
        return false;
    }

    public int getIconCount(ServerPlayer player) {
        return 10;
    }

    public int getIconSpacing(ServerPlayer player) {
        return -2;
    }

    public int getSliceIndex(ServerPlayer player) {
        return 0;
    }

    @Override
    public Component getBarComponent(ServerPlayer player) {
        int layerCount = getLayerCount(player);
        if (layerCount <= 0) {
            return Component.empty();
        }

        int iconCount = getIconCount(player);
        int sliceIndex = getSliceIndex(player);
        int spacing = getIconSpacing(player);
        boolean reversed = isOrderReversed(player);

        int containerIdx = getContainerTextureIndex(player, 0);
        if (containerIdx < 0) containerIdx = 0;

        MutableComponent bar = Component.empty();
        MutableComponent containers = renderIconRow(iconCount * 2, containerIdx, containerIdx, sliceIndex, iconCount, spacing, reversed);
        MutableComponent resetti = SpaceBuilder.getSpaceComponent(-ComponentWidthCalculator.calculateWidth(containers, false));

        bar.append(containers);

        for (int layer = 0; layer < layerCount; layer++) {
            if (!shouldDrawLayer(player, layer)) {
                continue;
            }

            double scale = Math.max(0.0001, getScale(player, layer));
            double rawVal = getValue(player, layer);
            double rawMaxVal = getMaxValue(player, layer);

            int valueUnits = (int) Math.ceil(rawVal / scale);
            int maxValueUnits = (int) Math.ceil(rawMaxVal / scale);

            int fullIdx = getFullTextureIndex(player, layer);
            int halfIdx = getHalfTextureIndex(player, layer);
            boolean tiered = isTieredOverflowEnabled(player, layer);

            bar.append(resetti.copy());
            bar.append(renderLayerUnits(valueUnits, maxValueUnits, fullIdx, halfIdx, sliceIndex, resetti, iconCount, spacing, tiered, reversed));
        }

        return bar;
    }

    protected MutableComponent renderLayerUnits(int valueUnits, int maxUnits, int fullIdx, int halfIdx, int sliceIdx, Component resetti, int iconCount, int spacing, boolean enableTieredOverflow, boolean reversed) {
        MutableComponent bar = Component.empty();
        int capacityPerTier = iconCount * 2;

        if (enableTieredOverflow && valueUnits > capacityPerTier) {
            int totalTiers = (int) Math.ceil((double) maxUnits / capacityPerTier);
            int tiersBelow = valueUnits / capacityPerTier;
            int colorTemp = (int) (((double) totalTiers - tiersBelow) / totalTiers * 0xAA + 0x44);
            int color = (colorTemp << 16) + (colorTemp << 8) + colorTemp;

            MutableComponent belowBar = renderIconRow(capacityPerTier, fullIdx, fullIdx, sliceIdx, iconCount, spacing, reversed).withColor(color);
            bar.append(belowBar);
            bar.append(resetti.copy());

            valueUnits %= capacityPerTier;
            if (valueUnits == 0) valueUnits = capacityPerTier;
        }

        bar.append(renderIconRow(valueUnits, fullIdx, halfIdx, sliceIdx, iconCount, spacing, reversed));
        return bar;
    }

    protected MutableComponent renderIconRow(int valueUnits, int fullIdx, int halfIdx, int sliceIdx, int iconCount, int spacing, boolean reversed) {
        int fullCount = valueUnits / 2;
        boolean hasHalf = (valueUnits % 2) != 0;
        int drawnIcons = fullCount + (hasHalf ? 1 : 0);
        int emptyIcons = iconCount - drawnIcons;
        int slotAdvance = 10 + spacing;

        MutableComponent layer = Component.empty();

        if (reversed) {
            if (emptyIcons > 0) {
                layer.append(SpaceBuilder.getSpaceComponent(emptyIcons * slotAdvance));
            }
            boolean firstDrawn = true;
            if (hasHalf) {
                layer.append(getSliceComponent(halfIdx, sliceIdx));
                firstDrawn = false;
            }
            for (int i = 0; i < fullCount; i++) {
                if (!firstDrawn && spacing != 0) {
                    layer.append(SpaceBuilder.getSpaceComponent(spacing));
                }
                layer.append(getSliceComponent(fullIdx, sliceIdx));
                firstDrawn = false;
            }
        } else {
            boolean firstDrawn = true;
            for (int i = 0; i < fullCount; i++) {
                if (!firstDrawn && spacing != 0) {
                    layer.append(SpaceBuilder.getSpaceComponent(spacing));
                }
                layer.append(getSliceComponent(fullIdx, sliceIdx));
                firstDrawn = false;
            }
            if (hasHalf) {
                if (!firstDrawn && spacing != 0) {
                    layer.append(SpaceBuilder.getSpaceComponent(spacing));
                }
                layer.append(getSliceComponent(halfIdx, sliceIdx));
            }
            if (emptyIcons > 0) {
                layer.append(SpaceBuilder.getSpaceComponent(emptyIcons * slotAdvance));
            }
        }

        return layer;
    }
}
