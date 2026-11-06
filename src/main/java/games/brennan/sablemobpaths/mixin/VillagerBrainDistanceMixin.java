package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RingBell;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.SetHiddenState;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.ai.behavior.SocializeAtBell;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Every brain behaviour that measures from a remembered block to the mob. Most of these run in
 * lambdas, so each wrap covers the whole class. Pairs in one frame pass through unchanged.
 */
@Mixin({
    AssignProfessionFromJobSite.class, GoToTargetLocation.class, HarvestFarmland.class, InteractWithDoor.class,
    LocateHidingPlace.class, MoveToTargetSink.class, RingBell.class, SetClosestHomeAsWalkTarget.class,
    SetHiddenState.class, SetWalkTargetFromBlockMemory.class, SleepInBed.class, SocializeAtBell.class,
    StrollAroundPoi.class, StrollToPoi.class, StrollToPoiList.class, UseBonemeal.class, ValidateNearbyPoi.class,
    WorkAtPoi.class
})
public abstract class VillagerBrainDistanceMixin {

    @WrapOperation(method = "*", require = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/BlockPos;closerToCenterThan(Lnet/minecraft/core/Position;D)Z"))
    private static boolean sablemobpaths$closerToCenterThan(BlockPos self, Position other, double distance,
                                                            Operation<Boolean> original) {
        return original.call(self, VillagerFrame.alignPosition(self, other), distance);
    }

    @WrapOperation(method = "*", require = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"))
    private static boolean sablemobpaths$closerThan(BlockPos self, Vec3i other, double distance,
                                                    Operation<Boolean> original) {
        return original.call(VillagerFrame.alignSelf(self, other), VillagerFrame.alignOther(other, self), distance);
    }

    @WrapOperation(method = "*", require = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/BlockPos;distManhattan(Lnet/minecraft/core/Vec3i;)I"))
    private static int sablemobpaths$distManhattan(BlockPos self, Vec3i other, Operation<Integer> original) {
        return original.call(VillagerFrame.alignSelf(self, other), VillagerFrame.alignOther(other, self));
    }

    @WrapOperation(method = "*", require = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D"))
    private static double sablemobpaths$distSqr(BlockPos self, Vec3i other, Operation<Double> original) {
        return original.call(VillagerFrame.alignSelf(self, other), VillagerFrame.alignOther(other, self));
    }
}
