package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.sensing.NearestBedSensor;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Behaviours that search for POIs or scan blocks around the mob. A carried mob searches its
 * carrier's plot, as Sable's own EatBlockGoalMixin does for grass. The owner is left off the target
 * because each class calls {@code blockPosition()} through a different entity type.
 */
@Mixin({
    AcquirePoi.class, HarvestFarmland.class, LocateHidingPlace.class, NearestBedSensor.class,
    SecondaryPoiSensor.class, SetClosestHomeAsWalkTarget.class, UseBonemeal.class, VillagerMakeLove.class
})
public abstract class VillagerBrainPositionMixin {

    @ModifyExpressionValue(method = "*", require = 0, at = @At(value = "INVOKE",
        target = "blockPosition()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sablemobpaths$searchInCarrierFrame(BlockPos world) {
        return VillagerFrame.inCarrierFrame(world);
    }
}
