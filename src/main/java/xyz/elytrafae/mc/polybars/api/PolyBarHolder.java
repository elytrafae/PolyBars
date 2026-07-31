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
 * Instances of this class are managed internally by the registry.
 */
public class PolyBarHolder {

    private final Identifier id;
    private final PolyBarSide preferredSide;
    private final int priority;
    private final List<PolyBar> bars = new ArrayList<>();

    private PolyBarSide assignedSide;
    private int assignedRow = -1;

    /**
     * Internal package-private constructor.
     * Users should not instantiate PolyBarHolder directly; use {@link PolyBarsApi#registerBarHolder(Identifier, PolyBarSide, int)}
     * or {@link PolyBarRegistry#registerBarHolder(Identifier, PolyBarSide, int)} instead.
     *
     * @param id            Unique identifier for the bar holder
     * @param preferredSide Preferred side of the hotbar (LEFT or RIGHT)
     * @param priority      Holder priority for row ordering and side equalization
     */
    PolyBarHolder(Identifier id, PolyBarSide preferredSide, int priority) {
        this.id = Objects.requireNonNull(id, "PolyBarHolder ID cannot be null");
        this.preferredSide = Objects.requireNonNull(preferredSide, "PolyBarHolder preferredSide cannot be null");
        this.priority = priority;
    }

    /**
     * Returns the unique identifier of this bar holder.
     *
     * @return Unique Identifier
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Returns the preferred side requested for this bar holder.
     * <p>
     * <b>Note:</b> This method should not be used for determining which side the bar will actually be rendered on.
     * Use {@link #getAssignedSide()} instead, as side equalization may reassign the holder to the opposite side.
     * </p>
     *
     * @return Preferred side (LEFT or RIGHT)
     */
    public PolyBarSide getPreferredSide() {
        return preferredSide;
    }

    /**
     * Returns the priority of this holder used for layout ordering and side equalization.
     * Higher priority holders are placed in lower row indices.
     *
     * @return Priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the final assigned side for rendering this bar holder after layout calculation.
     * This is the authoritative side indicator for where the holder will be drawn.
     *
     * @return Final assigned side (LEFT or RIGHT)
     */
    public PolyBarSide getAssignedSide() {
        return assignedSide != null ? assignedSide : preferredSide;
    }

    /**
     * Sets the final assigned side for rendering. Called internally during layout calculation.
     *
     * @param side Assigned PolyBarSide
     */
    public void setAssignedSide(PolyBarSide side) {
        this.assignedSide = side;
    }

    /**
     * Returns the assigned 0-indexed row position for rendering this bar holder.
     * Returns -1 if layout has not yet been calculated.
     *
     * @return Assigned row index (0-indexed) or -1 if unassigned
     */
    public int getAssignedRow() {
        return assignedRow;
    }

    /**
     * Sets the assigned row position for rendering. Called internally during layout calculation.
     *
     * @param row Assigned 0-indexed row position
     */
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
