package xyz.elytrafae.mc.polybars.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Public interface representing a custom HUD bar registered with PolyBars.
 * Bars are contained within a {@link PolyBarHolder} which defines their on-screen row and side layout.
 */
public interface PolyBar {

    /**
     * Unique identifier for this bar (e.g., "mymod:mana_bar").
     */
    Identifier getId();

    /**
     * Returns the parent PolyBarHolder that contains this bar.
     */
    PolyBarHolder getHolder();

    /**
     * List of input texture assets and their respective slice counts for this bar.
     */
    List<PolyBarTexture> getTextures();

    /**
     * Priority for selecting active bar within its parent PolyBarHolder.
     * When multiple bars in a holder are visible, the one with the highest priority is drawn.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Target height in GUI pixels for rendering this bar in Minecraft font system.
     * Defaults to 9 pixels to match vanilla Minecraft heart icon proportions (9x9px).
     */
    default int getTargetHeight() {
        return 9;
    }

    /**
     * Indicates whether the bar should be rendered for the given player on the current tick.
     *
     * @param player Target server player
     * @return true if the bar should be drawn, false otherwise
     */
    boolean shouldDraw(ServerPlayer player);

    /**
     * Returns the assembled text component representing this bar's current state for the player.
     *
     * @param player Target server player
     * @return Component to render
     */
    Component getBarComponent(ServerPlayer player);

    /**
     * Called by PolyBars after dynamic font creation to assign the generated unicode glyph characters.
     * The outer list matches the position of each texture in {@link #getTextures()},
     * and the inner list contains the unicode characters allocated for each slice of that texture.
     *
     * @param glyphsPerTexture Allocated glyphs grouped by input texture index
     */
    default void onGlyphsAssigned(List<List<Character>> glyphsPerTexture) {}
}
