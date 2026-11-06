package games.brennan.sablemobpaths;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Plot and world coordinates for brain code, which remembers blocks by position and measures to them
 * from the mob's world position. A block on a sub-level has a plot position far from anywhere.
 *
 * <p>Brain code has no level to hand at the call sites that matter, so the brain's tick holds one
 * here. Server thread only. Outside a brain tick everything answers unchanged.</p>
 */
public final class VillagerFrame {

    /** Vanilla never reports a village further away than this many sections. */
    private static final int MAX_SECTIONS_TO_VILLAGE = 6;

    @Nullable private static Level level;
    @Nullable private static Entity subject;

    private VillagerFrame() {}

    public static void enter(Level tickLevel, Entity entity) {
        level = tickLevel;
        subject = entity;
    }

    public static void exit() {
        level = null;
        subject = null;
    }

    /** {@code world} in the frame of the sub-level carrying the ticking mob, if one is. */
    public static BlockPos inCarrierFrame(BlockPos world) {
        if (subject == null) return world;
        SubLevel carrier = Sable.HELPER.getTrackingSubLevel(subject);
        if (carrier == null) return world;
        Vec3 at = world.equals(subject.blockPosition()) ? subject.position() : world.getCenter();
        return BlockPos.containing(carrier.logicalPose().transformPositionInverse(at));
    }

    /** {@code pos} as seen from {@code other}: unchanged unless pos is the world end of a mixed pair. */
    public static BlockPos alignSelf(BlockPos pos, Vec3i other) {
        return level == null ? pos : BlockPos.containing(align(pos, other));
    }

    /** {@code other} as seen from {@code pos}: unchanged unless other is the world end of a mixed pair. */
    public static Vec3i alignOther(Vec3i other, BlockPos pos) {
        return level == null ? other : BlockPos.containing(align(other, pos));
    }

    private static Vec3 align(Vec3i pos, Vec3i other) {
        SubLevel own = Sable.HELPER.getContaining(level, pos);
        SubLevel theirs = Sable.HELPER.getContaining(level, other);
        if (!PathRoute.mixedFrames(own, theirs) || theirs == null) return Vec3.atCenterOf(pos);
        return theirs.logicalPose().transformPositionInverse(Vec3.atCenterOf(pos));
    }

    /** A world position measured against plot position {@code pos} moves into that plot's frame. */
    public static Position alignPosition(BlockPos pos, Position other) {
        if (level == null) return other;
        SubLevel frame = Sable.HELPER.getContaining(level, pos);
        if (frame == null || Sable.HELPER.getContaining(level, other) != null) return other;
        return frame.logicalPose().transformPositionInverse(new Vec3(other.x(), other.y(), other.z()));
    }

    /** A plot position as a world one, for code that aims from the mob's own world position. */
    public static Vec3 inWorld(Vec3 pos) {
        return level == null ? pos : Sable.HELPER.projectOutOfSubLevel(level, pos);
    }

    /** Vanilla's answer for {@code section}, or nearer if a nearby sub-level holds a closer village. */
    public static int sectionsToVillage(ServerLevel serverLevel, SectionPos section, int vanilla) {
        if (vanilla == 0 || Sable.HELPER.getContaining(serverLevel, section) != null) return vanilla;
        Vec3 centre = Vec3.atCenterOf(section.center());
        double reach = MAX_SECTIONS_TO_VILLAGE * (double) SectionPos.SECTION_SIZE;
        BoundingBox3d box = new BoundingBox3d(centre.x - reach, centre.y - reach, centre.z - reach,
            centre.x + reach, centre.y + reach, centre.z + reach);
        int best = vanilla;
        for (SubLevel subLevel : Sable.HELPER.getAllIntersecting(serverLevel, box)) {
            if (subLevel.isRemoved()) continue;
            BlockPos local = BlockPos.containing(subLevel.logicalPose().transformPositionInverse(centre));
            best = Math.min(best, serverLevel.getPoiManager().sectionsToVillage(SectionPos.of(local)));
            if (best == 0) break;
        }
        return best;
    }
}
