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
    void boardingLegStepsAcrossTravelUntilAboard() {
        // Frame drifts along x; deck is z <= 6, mob stands at z = 9 and the search boards at x = 12.
        var leg = PathRoute.boardingLeg(new int[] {4, 0, 9}, new int[] {12, 0, 6}, true,
            (x, y, z) -> y == 0, (x, y, z) -> z <= 6);
        assertTrue(leg != null && leg.size() == 3);
        for (int[] cell : leg) assertTrue(cell[0] == 4);
        assertTrue(leg.get(2)[2] == 6);
    }

    @Test
    void boardingLegClimbsOneStepOntoAHigherDeck() {
        var leg = PathRoute.boardingLeg(new int[] {0, 0, 3}, new int[] {5, 1, 1}, true,
            (x, y, z) -> y == (z <= 1 ? 1 : 0), (x, y, z) -> z <= 1);
        assertTrue(leg != null && leg.get(leg.size() - 1)[1] == 1);
    }

    @Test
    void boardingLegGivesUpWhenBlockedOrAlreadyLevel() {
        assertTrue(PathRoute.boardingLeg(new int[] {0, 0, 9}, new int[] {5, 0, 6}, true,
            (x, y, z) -> z != 8, (x, y, z) -> z <= 6) == null);
        assertTrue(PathRoute.boardingLeg(new int[] {0, 0, 6}, new int[] {5, 0, 6}, true,
            (x, y, z) -> true, (x, y, z) -> true) == null);
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
    void onlyAPlotAndAWorldPositionAreMixed() {
        assertTrue(PathRoute.mixedFrames(DECK_A, null));
        assertTrue(PathRoute.mixedFrames(null, DECK_A));
        assertFalse(PathRoute.mixedFrames(null, null));
        assertFalse(PathRoute.mixedFrames(DECK_A, DECK_A));
    }

    @Test
    void searchesAreThrottledPerMobAndPerTick() {
        assertFalse(PathRoute.searchAllowed(100, 95, 0, false));
        assertTrue(PathRoute.searchAllowed(100, 90, 0, false));
        assertFalse(PathRoute.searchAllowed(100, 90, PathRoute.MAX_SEARCHES_PER_TICK, false));
    }

    @Test
    void aNewDestinationWaitsLessButStaysCapped() {
        assertFalse(PathRoute.searchAllowed(100, 99, 0, true));
        assertTrue(PathRoute.searchAllowed(100, 95, 0, true));
        assertFalse(PathRoute.searchAllowed(100, 95, PathRoute.MAX_SEARCHES_PER_TICK, true));
    }

    @Test
    void aPathIsRenewedOnlyAfterItHasBeenWalkedAWhile() {
        assertFalse(PathRoute.pathDueForRenewal(100, 100 - PathRoute.TICKS_BETWEEN_PATH_RENEWALS + 1));
        assertTrue(PathRoute.pathDueForRenewal(100, 100 - PathRoute.TICKS_BETWEEN_PATH_RENEWALS));
    }

    @Test
    void aDriftingDestinationIsNotANewOne() {
        assertFalse(PathRoute.isNewTarget(PathRoute.NEW_TARGET_DISTANCE * PathRoute.NEW_TARGET_DISTANCE));
        assertTrue(PathRoute.isNewTarget(PathRoute.NEW_TARGET_DISTANCE * PathRoute.NEW_TARGET_DISTANCE + 1));
    }
}
