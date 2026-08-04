package xyz.elytrafae.mc.polybars.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class with multi-texture support and glyph lookup utilities for PolyBars.
 */
public abstract class AbstractPolyBar implements PolyBar {

    private final Identifier id;
    private final List<PolyBarTexture> textures;
    private final int priority;
    private PolyBarHolder holder;

    private List<List<Character>> assignedGlyphsPerTexture = Collections.emptyList();

    public AbstractPolyBar(Identifier id, List<PolyBarTexture> textures, int priority) {
        if (textures == null || textures.isEmpty()) {
            throw new IllegalArgumentException("PolyBar must have at least one BarTexture");
        }
        this.id = id;
        this.textures = List.copyOf(textures);
        this.priority = priority;
    }

    public AbstractPolyBar(Identifier id, PolyBarTexture texture, int priority) {
        this(id, List.of(texture), priority);
    }

    public AbstractPolyBar(Identifier id, Identifier textureId, int slicesCount, PolyTextureSliceMode sliceMode, int priority) {
        this(id, new PolyBarTexture(textureId, slicesCount, sliceMode), priority);
    }

    public AbstractPolyBar(Identifier id, Identifier textureId, int slicesCount) {
        this(id, textureId, slicesCount, PolyTextureSliceMode.INDIVIDUAL, 0);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public PolyBarHolder getHolder() {
        return holder;
    }

    public void setHolder(PolyBarHolder holder) {
        this.holder = holder;
    }

    @Override
    public List<PolyBarTexture> getTextures() {
        return textures;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void onGlyphsAssigned(List<List<Character>> glyphsPerTexture) {
        if (glyphsPerTexture == null) {
            this.assignedGlyphsPerTexture = Collections.emptyList();
            return;
        }
        List<List<Character>> copy = new ArrayList<>();
        for (List<Character> list : glyphsPerTexture) {
            copy.add(list != null ? List.copyOf(list) : Collections.emptyList());
        }
        this.assignedGlyphsPerTexture = Collections.unmodifiableList(copy);
    }

    @Override
    public boolean shouldDraw(ServerPlayer player) {
        return !(player.isCreative() || player.isSpectator());
    }

    /**
     * Returns the allocated unicode character for a specific input texture index and slice index.
     *
     * @param textureIndex 0-indexed position in {@link #getTextures()}
     * @param sliceIndex   0-indexed slice position
     * @return Character glyph, or ' ' if unassigned or out of bounds
     */
    public char getSliceCharacter(int textureIndex, int sliceIndex) {
        if (assignedGlyphsPerTexture == null || textureIndex < 0 || textureIndex >= assignedGlyphsPerTexture.size()) {
            return ' ';
        }
        List<Character> glyphs = assignedGlyphsPerTexture.get(textureIndex);
        if (glyphs == null || sliceIndex < 0 || sliceIndex >= glyphs.size()) {
            return ' ';
        }
        return glyphs.get(sliceIndex);
    }

    /**
     * Single-texture overload returning the slice character for texture index 0.
     */
    public char getSliceCharacter(int sliceIndex) {
        return getSliceCharacter(0, sliceIndex);
    }

    /**
     * Returns a Component containing the glyph for a specific texture and slice index, formatted with this bar row's font.
     *
     * @param textureIndex 0-indexed position in {@link #getTextures()}
     * @param sliceIndex   0-indexed slice position
     * @return Formatted MutableComponent
     */
    public MutableComponent getSliceComponent(int textureIndex, int sliceIndex) {
        char ch = getSliceCharacter(textureIndex, sliceIndex);
        int rowIndex = holder != null ? holder.getAssignedRow() : 0;
        if (rowIndex < 0) rowIndex = 0;
        Identifier fontId = Identifier.fromNamespaceAndPath("polybars", "row_" + rowIndex);
        MutableComponent comp = Component.literal(String.valueOf(ch));
        comp.withStyle(style -> style.withoutShadow().withFont(new FontDescription.Resource(fontId)));
        return comp;
    }

    /**
     * Single-texture overload returning the formatted slice component for texture index 0.
     */
    public MutableComponent getSliceComponent(int sliceIndex) {
        return getSliceComponent(0, sliceIndex);
    }

    public List<List<Character>> getAssignedGlyphsPerTexture() {
        return assignedGlyphsPerTexture;
    }
}
