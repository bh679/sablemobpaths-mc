package games.brennan.sablemobpaths;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import games.brennan.sablemobpaths.mixin.PathTargetAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Runs one path search that crosses between the world and sub-levels. */
public final class CrossFrameSearch {

    private static long budgetTick = Long.MIN_VALUE;
    private static int spentThisTick;

    private CrossFrameSearch() {}

    /** The first sub-level whose plot holds one of {@code targets}, else {@code null} for the world. */
    @Nullable
    public static SubLevel destinationFrame(Level level, Set<BlockPos> targets) {
        for (BlockPos target : targets) {
            SubLevel frame = Sable.HELPER.getContaining(level, target);
            if (frame != null) return frame;
        }
        return null;
    }

    /**
     * The sub-level lying between a mob and a world destination, near the height of either end, so
     * the search can cross it or go round it. A sub-level far overhead doesn't count.
     */
    @Nullable
    public static SubLevel inTheWay(Level level, Mob mob, Set<BlockPos> targets) {
        Vec3 from = mob.position();
        for (BlockPos target : targets) {
            BoundingBox3d corridor = new BoundingBox3d(
                Math.min(from.x, target.getX()) - 1, Math.min(from.y, target.getY()) - 2, Math.min(from.z, target.getZ()) - 1,
                Math.max(from.x, target.getX()) + 1, Math.max(from.y, target.getY()) + 2, Math.max(from.z, target.getZ()) + 1);
            for (SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, corridor)) {
                if (!subLevel.isRemoved()) return subLevel;
            }
        }
        return null;
    }

    /**
     * The sub-level with a block at, or just under, the world position {@code target}. Has to be
     * asked before vanilla scans down from a target that reads as air, which would lose the deck.
     */
    @Nullable
    public static SubLevel standingOn(Level level, BlockPos target) {
        Vec3 world = target.getCenter();
        BoundingBox3d probe = new BoundingBox3d(world.x, world.y - 1.0, world.z, world.x, world.y, world.z);
        for (SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, probe)) {
            if (subLevel.isRemoved()) continue;
            BlockPos local = BlockPos.containing(subLevel.logicalPose().transformPositionInverse(world));
            if (hasBlock(level, local) || hasBlock(level, local.below())) return subLevel;
        }
        return null;
    }

    private static boolean hasBlock(Level level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        return chunk != null && !chunk.getBlockState(pos).isAir();
    }

    /** Whether a world-coordinate destination sits inside the sub-level carrying the mob. */
    public static boolean insideMobFrame(@Nullable SubLevel mobFrame, Set<BlockPos> targets) {
        if (mobFrame == null) return false;
        for (BlockPos target : targets) {
            Vec3 c = target.getCenter();
            if (mobFrame.boundingBox().contains(c.x, c.y, c.z)) return true;
        }
        return false;
    }

    /** Spends one search from this tick's budget if {@code last} allows it. */
    public static boolean trySpend(long now, long last, boolean newTarget) {
        if (now != budgetTick) {
            budgetTick = now;
            spentThisTick = 0;
        }
        if (!PathRoute.searchAllowed(now, last, spentThisTick, newTarget)) return false;
        spentThisTick++;
        return true;
    }

    /**
     * Searches in a sub-level's frame: the destination's if it has one, else the one carrying the
     * mob. The stretch that matters stays put relative to whatever it stands on while the sub-level
     * moves. Sable's start-node hooks read the mob's position through its tracking sub-level, so
     * that is pointed at the frame for the length of the search.
     */
    @Nullable
    public static Path search(Level level, Mob mob, PathFinder finder, Set<BlockPos> targets,
                              SubLevel frame, boolean fromAbove, int range, float maxDistance,
                              int reach, float visitedMultiplier) {
        Vec3 inFrame = frame.logicalPose().transformPositionInverse(mob.position());
        BlockPos start = BlockPos.containing(inFrame);
        BlockPos centre = fromAbove ? start.above() : start;

        Map<BlockPos, BlockPos> asked = new HashMap<>();
        for (BlockPos target : targets) {
            asked.put(Sable.HELPER.getContaining(level, target) == frame
                ? target
                : BlockPos.containing(frame.logicalPose().transformPositionInverse(target.getCenter())), target);
        }

        FrameRegion region = new FrameRegion(level, centre.offset(-range, -range, -range),
            centre.offset(range, range, range), frame, nearbyExcept(level, mob, range, frame));

        EntityMovementExtension moving = (EntityMovementExtension) mob;
        SubLevel carrier = Sable.HELPER.getTrackingSubLevel(mob);
        var lastCarrierId = moving.sable$getLastTrackingSubLevelID();
        moving.sable$setTrackingSubLevel(frame);
        try {
            Path found = finder.findPath(region, mob, asked.keySet(), maxDistance, reach, visitedMultiplier);
            // The caller looks up the target it asked for, such as a villager claiming a POI.
            if (found != null && found.getTarget() != null) {
                ((PathTargetAccessor) found).sablemobpaths$setTarget(asked.get(found.getTarget()));
            }
            return found;
        } finally {
            moving.sable$setTrackingSubLevel(carrier);
            moving.sable$setLastTrackingSubLevelID(lastCarrierId);
        }
    }

    private static List<SubLevel> nearbyExcept(Level level, Mob mob, int range, @Nullable SubLevel frame) {
        Vec3 p = mob.position();
        BoundingBox3d box = new BoundingBox3d(p.x - range, p.y - range, p.z - range,
            p.x + range, p.y + range, p.z + range);
        List<SubLevel> nearby = new ArrayList<>();
        for (SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, box)) {
            if (subLevel != frame && !subLevel.isRemoved()) nearby.add(subLevel);
        }
        return nearby;
    }
}
