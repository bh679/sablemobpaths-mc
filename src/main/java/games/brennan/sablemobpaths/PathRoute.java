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

    /** Whether exactly one of two positions is on a sub-level, so comparing them needs a conversion. */
    public static boolean mixedFrames(@Nullable Object frameA, @Nullable Object frameB) {
        return (frameA == null) != (frameB == null);
    }

    /**
     * Earliest game time a mob may run another cross-frame search after one at {@code last}. A mob
     * following something that moves waits the full gap; one sent somewhere new shouldn't stand idle.
     */
    public static boolean searchAllowed(long now, long last, int searchesThisTick, boolean newTarget) {
        long wait = newTarget ? MIN_TICKS_AFTER_NEW_TARGET : MIN_TICKS_BETWEEN_SEARCHES;
        return now - last >= wait && searchesThisTick < MAX_SEARCHES_PER_TICK;
    }

    /** Whether a destination is far enough from the last one asked for to be a new errand, not the same one drifting. */
    public static boolean isNewTarget(double squaredDistanceFromLast) {
        return squaredDistanceFromLast > NEW_TARGET_DISTANCE * NEW_TARGET_DISTANCE;
    }

    /** Whether a mob still walking a path has had it long enough to deserve a fresh search when it next asks. */
    public static boolean pathDueForRenewal(long now, long last) {
        return now - last >= TICKS_BETWEEN_PATH_RENEWALS;
    }

    public static final int MIN_TICKS_BETWEEN_SEARCHES = 10;
    public static final int TICKS_BETWEEN_PATH_RENEWALS = 20;
    public static final int MIN_TICKS_AFTER_NEW_TARGET = 2;
    /** Wider than a moving train carries a target between two searches, up to about 16 blocks/s. */
    public static final int NEW_TARGET_DISTANCE = 8;
    /** A burst cap, not the usual limit: the per-mob wait sets the average. Too low and mobs asking on the same tick starve in turn order. */
    public static final int MAX_SEARCHES_PER_TICK = 64;
}
