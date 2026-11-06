package games.brennan.sablemobpaths.mixin;

import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The fallback for a far or unreachable target aims from the mob's world position, so it needs a world target. */
@Mixin({MoveToTargetSink.class, SetWalkTargetFromBlockMemory.class})
public abstract class VillagerWalkTowardsMixin {

    @ModifyArg(method = "*", require = 0, index = 3, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/util/DefaultRandomPos;getPosTowards(Lnet/minecraft/world/entity/PathfinderMob;IILnet/minecraft/world/phys/Vec3;D)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 sablemobpaths$towardsInWorld(Vec3 target) {
        return VillagerFrame.inWorld(target);
    }
}
