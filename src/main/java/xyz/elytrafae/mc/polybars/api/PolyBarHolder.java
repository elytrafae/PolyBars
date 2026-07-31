package xyz.elytrafae.mc.polybars.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a holder for custom HUD bars.
 * A PolyBarHolder defines a preferred side and priority, and automatically gets assigned a final side and row position.
 * Multiple PolyBars can be registered to a single PolyBarHolder, with at most one active bar drawn at runtime per player.
 */
public class PolyBarHolder {

    private final Identifier id;
    private final PolyBarSide preferredSide;
    private final int priority;
    private final List<PolyBar> bars = new ArrayList<>();

    private PolyBarSide assignedSide;
    private int assignedRow = -1;

    public PolyBarHolder(Identifier id, PolyBarSide preferredSide, int priority) {
        this.id = Objects.requireNonNull(id, "PolyBarHolder ID cannot be null");
        this.preferredSide = Objects.requireNonNull(preferredSide, "PolyBarHolder preferredSide cannot be null");
        this.priority = priority;
    }

    public Identifier getId() {
        return id;
    }

    public PolyBarSide getPreferredSide() {
        return preferredSide;
    }

    public int getPriority() {
        return priority;
    }

    public PolyBarSide getAssignedSide() {
        return assignedSide != null ? assignedSide : preferredSide;
    }

    public void setAssignedSide(PolyBarSide side) {
        this.assignedSide = side;
    }

    public int getAssignedRow() {
        return assignedRow;
    }

    public void setAssignedRow(int row) {
        this.assignedRow = row;
    }

    /**
     * Registers a PolyBar to this holder. Sets the bar's parent holder reference.
     */
    public synchronized void addBar(PolyBar bar) {
        Objects.requireNonNull(bar, "PolyBar cannot be null");
        if (!bars.contains(bar)) {
            bars.add(bar);
            if (bar instanceof AbstractPolyBar abstractBar) {
                abstractBar.setHolder(this);
            }
        }
    }

    /**
     * Returns an unmodifiable view of all PolyBars registered to this holder.
     */
    public synchronized List<PolyBar> getBars() {
        return List.copyOf(bars);
    }

    /**
     * Evaluates all contained bars and returns the highest priority visible (non-hidden) bar for a player.
     *
     * @param player Target server player
     * @return Optional containing the active PolyBar, or empty if no bars are visible
     */
    public synchronized Optional<PolyBar> getActiveBar(ServerPlayer player) {
        return bars.stream()
                .filter(b -> b.shouldDraw(player))
                .max(Comparator.comparingInt(PolyBar::getPriority));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolyBarHolder polyBarHolder = (PolyBarHolder) o;
        return Objects.equals(id, polyBarHolder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
