package games.brennan.sablemobpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathRouteTest {

    private static final Object DECK_A = new Object();
    private static final Object DECK_B = new Object();

    @Test
    void sameFrameStaysWithSableOrVanilla() {
        assertFalse(PathRoute.crossesFrames(null, null, false));
        assertFalse(PathRoute.crossesFrames(DECK_A, DECK_A, false));
    }

    @Test
    void changingFrameIsOurs() {
        assertTrue(PathRoute.crossesFrames(null, DECK_A, false));
        assertTrue(PathRoute.crossesFrames(DECK_A, null, false));
        assertTrue(PathRoute.crossesFrames(DECK_A, DECK_B, false));
    }

    @Test
    void worldCoordinateOnTheMobsOwnDeckStaysWithSable() {
        assertFalse(PathRoute.crossesFrames(DECK_A, null, true));
    }

    @Test
    void searchesAreThrottledPerMobAndPerTick() {
        assertFalse(PathRoute.searchAllowed(100, 95, 0));
        assertTrue(PathRoute.searchAllowed(100, 90, 0));
        assertFalse(PathRoute.searchAllowed(100, 90, PathRoute.MAX_SEARCHES_PER_TICK));
    }
}
