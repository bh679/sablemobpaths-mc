package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A look target on a block in a plot would turn the mob's head toward the plot, far from anywhere. */
@Mixin(LookAtTargetSink.class)
public abstract class VillagerLookMixin {

    @ModifyExpressionValue(method = "*", require = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/behavior/PositionTracker;currentPosition()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 sablemobpaths$lookInWorld(Vec3 position) {
        return VillagerFrame.inWorld(position);
    }
}
