package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Staying asleep compares the mob's height with the bed's, which needs both in one frame. */
@Mixin(SleepInBed.class)
public abstract class SleepInBedHeightMixin {

    @ModifyExpressionValue(method = "canStillUse", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;getY()D"))
    private double sablemobpaths$heightInBedFrame(double y, @Local(argsOnly = true) LivingEntity entity,
                                                  @Local BlockPos bed) {
        return VillagerFrame.alignPosition(bed, entity.position()).y();
    }
}
