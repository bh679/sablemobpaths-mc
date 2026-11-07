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

    /** A cell test in a search's frame. */
    @FunctionalInterface
    public interface CellTest {
        boolean test(int x, int y, int z);
    }

    /**
     * Cells leading from a mob on still ground straight across a moving frame's direction of travel
     * until one is aboard. From still ground, walking along the frame's travel gains only the mob's
     * speed less the frame's, so a route that steps across first reaches the deck sooner. Each step
     * may rise or drop a block, as a walk can.
     *
     * @param travelAlongX whether the frame drifts mostly along its own x axis
     * @param toward       where the route first goes aboard; sets which way is across
     * @return the cells after {@code from}, the last aboard, or {@code null} if a straight line won't do
     */
    @Nullable
    public static java.util.List<int[]> boardingLeg(int[] from, int[] toward, boolean travelAlongX,
                                                    CellTest walkable, CellTest aboard) {
        int axis = travelAlongX ? 2 : 0;
        int gap = toward[axis] - from[axis];
        if (gap == 0) return null;
        int step = Integer.signum(gap);
        java.util.List<int[]> leg = new java.util.ArrayList<>();
        int[] at = from.clone();
        for (int i = 0; i < Math.abs(gap) + BOARDING_OVERSHOOT; i++) {
            int[] next = at.clone();
            next[axis] += step;
            int[] stood = null;
            for (int dy : new int[] {0, 1, -1}) {
                if (walkable.test(next[0], next[1] + dy, next[2])) {
                    stood = new int[] {next[0], next[1] + dy, next[2]};
                    break;
                }
            }
            if (stood == null) return null;
            leg.add(stood);
            if (aboard.test(stood[0], stood[1], stood[2])) return leg;
            at = stood;
        }
        return null;
    }

    /** Below this many blocks a tick a frame is as good as still, and the route it found stands. */
    public static final double MIN_DRIFT_PER_TICK = 0.02;
    /** How far past the searched boarding line a straight leg may look for the deck's edge. */
    public static final int BOARDING_OVERSHOOT = 2;
    public static final int MIN_TICKS_BETWEEN_SEARCHES = 10;
    public static final int TICKS_BETWEEN_PATH_RENEWALS = 20;
    public static final int MIN_TICKS_AFTER_NEW_TARGET = 2;
    /** Wider than a moving train carries a target between two searches, up to about 16 blocks/s. */
    public static final int NEW_TARGET_DISTANCE = 8;
    /** A burst cap, not the usual limit: the per-mob wait sets the average. Too low and mobs asking on the same tick starve in turn order. */
    public static final int MAX_SEARCHES_PER_TICK = 64;
}
