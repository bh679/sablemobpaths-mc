package games.brennan.sablemobpaths;

import org.jetbrains.annotations.Nullable;

/** Pure rule for which searches this mod takes over; everything else stays with Sable or vanilla. */
public final class PathRoute {

    private PathRoute() {}

    /**
     * @param mobFrame             sub-level carrying the mob, or {@code null} for the world
     * @param destFrame            sub-level holding the destination, or {@code null} for the world
     * @param destInsideMobFrame   a world-coordinate destination sits inside the mob's sub-level
     * @return whether the search has to cross a frame boundary
     */
    public static boolean crossesFrames(@Nullable Object mobFrame, @Nullable Object destFrame,
                                        boolean destInsideMobFrame) {
        if (mobFrame == destFrame) return false;
        return destFrame != null || !destInsideMobFrame;
    }

    /** Earliest game time a mob may run another cross-frame search after one at {@code last}. */
    public static boolean searchAllowed(long now, long last, int searchesThisTick) {
        return now - last >= MIN_TICKS_BETWEEN_SEARCHES && searchesThisTick < MAX_SEARCHES_PER_TICK;
    }

    public static final int MIN_TICKS_BETWEEN_SEARCHES = 10;
    /** A burst cap, not the usual limit: the per-mob wait sets the average. Too low and mobs asking on the same tick starve in turn order. */
    public static final int MAX_SEARCHES_PER_TICK = 64;
}
