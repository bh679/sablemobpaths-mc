package games.brennan.sablemobpaths;

import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One search's view of the world plus every nearby sub-level, in a single frame.
 *
 * <p>A cell in the frame reads as the frame's own block if it has one, else whatever occupies the
 * same place in the world or another sub-level. Reads never load a chunk and are cached for the
 * search, which lives for one call on the server thread.</p>
 */
public final class FrameRegion extends PathNavigationRegion {

    private static final BlockState EMPTY = Blocks.AIR.defaultBlockState();

    private final Level level;
    private final SubLevel frame;
    private final List<SubLevel> others;
    private final Long2ObjectOpenHashMap<BlockState> cells = new Long2ObjectOpenHashMap<>();

    /**
     * @param frame  the sub-level whose coordinates the search runs in
     * @param others sub-levels near the search other than {@code frame}
     */
    public FrameRegion(Level level, BlockPos from, BlockPos to, SubLevel frame, List<SubLevel> others) {
        super(level, from, to);
        this.level = level;
        this.frame = frame;
        this.others = others;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        long key = pos.asLong();
        BlockState cached = cells.get(key);
        if (cached == null) {
            cached = resolve(pos);
            cells.put(key, cached);
        }
        return cached;
    }

    private BlockState resolve(BlockPos pos) {
        BlockState own = read(pos.getX(), pos.getY(), pos.getZ());
        if (!own.isAir()) return own;

        Vec3 world = frame.logicalPose().transformPosition(Vec3.atCenterOf(pos));
        BlockState ground = read(Mth.floor(world.x), Mth.floor(world.y), Mth.floor(world.z));
        if (!ground.isAir()) return ground;

        for (SubLevel other : others) {
            if (!other.boundingBox().contains(world.x, world.y, world.z)) continue;
            Vec3 there = other.logicalPose().transformPositionInverse(world);
            BlockState block = read(Mth.floor(there.x), Mth.floor(there.y), Mth.floor(there.z));
            if (!block.isAir()) return block;
        }
        return EMPTY;
    }

    private BlockState read(int x, int y, int z) {
        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
        return chunk == null ? EMPTY : chunk.getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    /** The evaluator's clearance test, against this merged view rather than one frame's chunks. */
    @Override
    public boolean noCollision(@Nullable Entity entity, AABB box) {
        VoxelShape boxShape = Shapes.create(box);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX - 1.0E-7); x++) {
            for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY - 1.0E-7); y++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ - 1.0E-7); z++) {
                    pos.set(x, y, z);
                    VoxelShape shape = getBlockState(pos).getCollisionShape(this, pos);
                    if (!shape.isEmpty() && Shapes.joinIsNotEmpty(shape.move(x, y, z), boxShape, BooleanOp.AND)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
