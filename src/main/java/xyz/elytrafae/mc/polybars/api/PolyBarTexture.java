package xyz.elytrafae.mc.polybars.api;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * Represents a raw texture asset and its corresponding slice count for a PolyBar.
 *
 * @param textureId Namespaced ID pointing to the texture asset (e.g. "mymod:textures/gui/mana_bar.png")
 * @param slicesCount Number of slices the texture should be divided into
 * @param mode How the slices are created
 */
public record PolyBarTexture(Identifier textureId, int slicesCount, PolyTextureSliceMode mode) {

    public PolyBarTexture {
        Objects.requireNonNull(textureId, "BarTexture textureId cannot be null");
        if (slicesCount <= 0) {
            throw new IllegalArgumentException("BarTexture slicesCount must be > 0");
        }
    }

    /**
     * Helper constructor creating a BarTexture from a namespace and path string.
     */
    public static PolyBarTexture of(String namespace, String path, int slicesCount, PolyTextureSliceMode mode) {
        return new PolyBarTexture(Identifier.fromNamespaceAndPath(namespace, path), slicesCount, mode);
    }
}
