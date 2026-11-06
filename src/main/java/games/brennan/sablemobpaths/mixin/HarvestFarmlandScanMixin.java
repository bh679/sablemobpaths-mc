package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The crop scan builds each position from the villager's own coordinates, not its block position. */
@Mixin(HarvestFarmland.class)
public abstract class HarvestFarmlandScanMixin {

    @WrapOperation(method = "checkExtraStartConditions", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(DDD)Lnet/minecraft/core/BlockPos$MutableBlockPos;"))
    private BlockPos.MutableBlockPos sablemobpaths$scanInCarrierFrame(BlockPos.MutableBlockPos pos, double x, double y, double z,
                                                                      Operation<BlockPos.MutableBlockPos> original) {
        return pos.set(VillagerFrame.inCarrierFrame(BlockPos.containing(x, y, z)));
    }
}
