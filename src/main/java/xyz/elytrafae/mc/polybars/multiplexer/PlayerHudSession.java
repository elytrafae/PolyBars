package xyz.elytrafae.mc.polybars.multiplexer;

import net.minecraft.network.chat.Component;

public class PlayerHudSession {

    public static final int DEFAULT_VANILLA_ACTIONBAR_LIFESPAN_TICKS = 60;

    private Component cachedVanillaActionBar = null;
    private int vanillaActionBarTicksRemaining = 0;
    private Component lastSentComponent = null;

    public void setVanillaActionBar(Component text) {
        this.cachedVanillaActionBar = text;
        this.vanillaActionBarTicksRemaining = DEFAULT_VANILLA_ACTIONBAR_LIFESPAN_TICKS;
    }

    public void clearVanillaActionBar() {
        this.cachedVanillaActionBar = null;
        this.vanillaActionBarTicksRemaining = 0;
    }

    public Component getCachedVanillaActionBar() {
        if (vanillaActionBarTicksRemaining <= 0) {
            return null;
        }
        return cachedVanillaActionBar;
    }

    public void tick() {
        if (vanillaActionBarTicksRemaining > 0) {
            vanillaActionBarTicksRemaining--;
            if (vanillaActionBarTicksRemaining == 0) {
                cachedVanillaActionBar = null;
            }
        }
    }

    public Component getLastSentComponent() {
        return lastSentComponent;
    }

    public void setLastSentComponent(Component lastSentComponent) {
        this.lastSentComponent = lastSentComponent;
    }
}
