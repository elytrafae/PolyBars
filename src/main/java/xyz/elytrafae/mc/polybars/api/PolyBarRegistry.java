package xyz.elytrafae.mc.polybars.api;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for custom PolyBarHolders and PolyBars registered by mods during initialization.
 * Automatically performs layout positioning and side equalization across all holders.
 */
public class PolyBarRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("PolyBarRegistry");
    private static final Map<Identifier, PolyBarHolder> HOLDERS = new ConcurrentHashMap<>();

    private static boolean layoutCalculated = false;
    private static Map<Integer, List<PolyBarHolder>> cachedHoldersByRow = Collections.emptyMap();

    /**
     * Registers a custom PolyBarHolder with an ID, preferred side, and priority.
     *
     * @param id            Unique identifier for the bar holder
     * @param preferredSide Preferred side of the hotbar (LEFT or RIGHT)
     * @param priority      Holder priority for row ordering and side equalization
     */
    public static void registerBarHolder(Identifier id, PolyBarSide preferredSide, int priority) {
        Objects.requireNonNull(id, "PolyBarHolder ID cannot be null");
        Objects.requireNonNull(preferredSide, "PolyBarSide cannot be null");
        PolyBarHolder holder = new PolyBarHolder(id, preferredSide, priority);
        registerBarHolder(holder);
    }

    /**
     * Registers a custom PolyBarHolder instance.
     *
     * @param holder PolyBarHolder instance
     */
    public static void registerBarHolder(PolyBarHolder holder) {
        Objects.requireNonNull(holder, "PolyBarHolder instance cannot be null");
        Objects.requireNonNull(holder.getId(), "PolyBarHolder ID cannot be null");

        if (HOLDERS.putIfAbsent(holder.getId(), holder) != null) {
            LOGGER.warn("Duplicate PolyBarHolder registration ignored for ID: {}", holder.getId());
        } else {
            LOGGER.info("Registered PolyBarHolder: {} (Preferred Side: {}, Priority: {})",
                    holder.getId(), holder.getPreferredSide(), holder.getPriority());
        }
    }

    /**
     * Registers a PolyBar to a specific PolyBarHolder ID.
     * If the specified holder does not exist, a new PolyBarHolder with preferred side LEFT and priority 0 is created automatically.
     *
     * @param barHolderId Identifier of target PolyBarHolder
     * @param bar         PolyBar instance
     */
    public static void registerBar(Identifier barHolderId, PolyBar bar) {
        Objects.requireNonNull(barHolderId, "PolyBarHolder ID cannot be null");
        Objects.requireNonNull(bar, "PolyBar instance cannot be null");

        PolyBarHolder holder = HOLDERS.computeIfAbsent(barHolderId, id -> {
            PolyBarHolder newHolder = new PolyBarHolder(id, PolyBarSide.LEFT, 0);
            LOGGER.info("Auto-created PolyBarHolder for ID: {}", id);
            return newHolder;
        });

        holder.addBar(bar);
        LOGGER.info("Registered PolyBar '{}' to PolyBarHolder '{}'", bar.getId(), holder.getId());
    }

    /**
     * Registers a PolyBar directly. Uses bar.getHolder().getId() if available, or bar.getId().
     *
     * @param bar PolyBar instance
     */
    public static void registerBar(PolyBar bar) {
        Objects.requireNonNull(bar, "PolyBar instance cannot be null");
        if (bar.getHolder() != null) {
            registerBarHolder(bar.getHolder());
            bar.getHolder().addBar(bar);
        } else {
            registerBar(bar.getId(), bar);
        }
    }

    /**
     * Returns an optional containing the PolyBarHolder registered under the specified ID.
     */
    public static Optional<PolyBarHolder> getHolder(Identifier id) {
        return Optional.ofNullable(HOLDERS.get(id));
    }

    /**
     * Returns an optional containing the PolyBar registered under the specified ID.
     */
    public static Optional<PolyBar> getBar(Identifier id) {
        if (id == null) return Optional.empty();
        for (PolyBarHolder holder : HOLDERS.values()) {
            for (PolyBar bar : holder.getBars()) {
                if (id.equals(bar.getId())) {
                    return Optional.of(bar);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all registered PolyBars across all PolyBarHolders.
     */
    public static Collection<PolyBar> getAllBars() {
        List<PolyBar> all = new ArrayList<>();
        for (PolyBarHolder holder : HOLDERS.values()) {
            all.addAll(holder.getBars());
        }
        return Collections.unmodifiableCollection(all);
    }

    /**
     * Returns an unmodifiable collection of all registered PolyBarHolders.
     */
    public static Collection<PolyBarHolder> getAllHolders() {
        return Collections.unmodifiableCollection(HOLDERS.values());
    }

    /**
     * Calculates automatic position assignments (assigned side and assigned row) for all registered PolyBarHolders.
     * Enforces priority placement and side equalization so that |leftCount - rightCount| <= 1.
     * Calculated only ONCE; subsequent calls immediately return cached layout.
     */
    public static synchronized void calculateLayout() {
        if (layoutCalculated) return;

        if (HOLDERS.isEmpty()) {
            cachedHoldersByRow = Collections.emptyMap();
            layoutCalculated = true;
            return;
        }

        List<PolyBarHolder> allHolders = new ArrayList<>(HOLDERS.values());
        // Sort all holders by priority descending (highest priority first)
        allHolders.sort(Comparator.comparingInt(PolyBarHolder::getPriority).reversed());

        List<PolyBarHolder> leftList = new ArrayList<>();
        List<PolyBarHolder> rightList = new ArrayList<>();

        // Step 1: Initial distribution based on preferred side
        for (PolyBarHolder holder : allHolders) {
            if (holder.getPreferredSide() == PolyBarSide.LEFT) {
                leftList.add(holder);
            } else {
                rightList.add(holder);
            }
        }

        // Step 2: Side Equalization - Ensure |leftCount - rightCount| <= 1 by moving lowest priority holder from taller side
        while (Math.abs(leftList.size() - rightList.size()) >= 2) {
            if (leftList.size() > rightList.size()) {
                // Remove lowest priority holder from left (last element) and move to right
                PolyBarHolder moved = leftList.remove(leftList.size() - 1);
                rightList.add(moved);
                rightList.sort(Comparator.comparingInt(PolyBarHolder::getPriority).reversed());
            } else {
                // Remove lowest priority holder from right (last element) and move to left
                PolyBarHolder moved = rightList.remove(rightList.size() - 1);
                leftList.add(moved);
                leftList.sort(Comparator.comparingInt(PolyBarHolder::getPriority).reversed());
            }
        }

        // Step 3: Assign final row and side to left side holders
        for (int row = 0; row < leftList.size(); row++) {
            PolyBarHolder holder = leftList.get(row);
            holder.setAssignedSide(PolyBarSide.LEFT);
            holder.setAssignedRow(row);
            LOGGER.info("Assigned PolyBarHolder '{}' -> Side: LEFT, Row: {}", holder.getId(), row);
        }

        // Step 4: Assign final row and side to right side holders
        for (int row = 0; row < rightList.size(); row++) {
            PolyBarHolder holder = rightList.get(row);
            holder.setAssignedSide(PolyBarSide.RIGHT);
            holder.setAssignedRow(row);
            LOGGER.info("Assigned PolyBarHolder '{}' -> Side: RIGHT, Row: {}", holder.getId(), row);
        }

        Map<Integer, List<PolyBarHolder>> grouped = new TreeMap<>();
        for (PolyBarHolder holder : HOLDERS.values()) {
            if (holder.getAssignedRow() >= 0) {
                grouped.computeIfAbsent(holder.getAssignedRow(), r -> new ArrayList<>()).add(holder);
            }
        }
        cachedHoldersByRow = Collections.unmodifiableMap(grouped);
        layoutCalculated = true;
    }

    /**
     * Returns all PolyBarHolders grouped by assigned row index (ascending).
     * Calculates layout ONCE if not already calculated.
     */
    public static Map<Integer, List<PolyBarHolder>> getHoldersByRowOrdered() {
        if (!layoutCalculated) {
            calculateLayout();
        }
        return cachedHoldersByRow;
    }

}
