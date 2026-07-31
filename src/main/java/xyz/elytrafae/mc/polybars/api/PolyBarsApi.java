package xyz.elytrafae.mc.polybars.api;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Optional;

/**
 * Public API entry point for PolyBars.
 */
public final class PolyBarsApi {

    private PolyBarsApi() {}

    /**
     * Registers a custom PolyBarHolder with an ID, preferred side, and priority.
     *
     * @param id            Unique identifier for the bar holder
     * @param preferredSide Preferred side of the hotbar (LEFT or RIGHT)
     * @param priority      Holder priority for row ordering and side equalization
     */
    public static void registerBarHolder(Identifier id, PolyBarSide preferredSide, int priority) {
        PolyBarRegistry.registerBarHolder(id, preferredSide, priority);
    }

    /**
     * Registers a custom PolyBarHolder instance.
     *
     * @param holder The PolyBarHolder instance to register
     */
    public static void registerBarHolder(PolyBarHolder holder) {
        PolyBarRegistry.registerBarHolder(holder);
    }

    /**
     * Registers a PolyBar to a specific PolyBarHolder ID.
     *
     * @param barHolderId Identifier of target PolyBarHolder
     * @param bar         The PolyBar instance to register
     */
    public static void registerBar(Identifier barHolderId, PolyBar bar) {
        PolyBarRegistry.registerBar(barHolderId, bar);
    }

    /**
     * Registers a custom PolyBar instance.
     *
     * @param bar The PolyBar instance to register
     */
    public static void registerBar(PolyBar bar) {
        PolyBarRegistry.registerBar(bar);
    }

    /**
     * Retrieves a registered PolyBarHolder by its unique identifier.
     */
    public static Optional<PolyBarHolder> getHolder(Identifier id) {
        return PolyBarRegistry.getHolder(id);
    }

    /**
     * Returns all currently registered PolyBarHolders.
     */
    public static Collection<PolyBarHolder> getAllHolders() {
        return PolyBarRegistry.getAllHolders();
    }

    /**
     * Retrieves a registered PolyBar by its unique identifier.
     */
    public static Optional<PolyBar> getBar(Identifier id) {
        return PolyBarRegistry.getBar(id);
    }

    /**
     * Returns all currently registered PolyBars.
     */
    public static Collection<PolyBar> getAllBars() {
        return PolyBarRegistry.getAllBars();
    }
}
