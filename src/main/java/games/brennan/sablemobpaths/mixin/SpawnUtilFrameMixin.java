package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A mob checked at a plot position still has to enter the world where that plot is. */
@Mixin(SpawnUtil.class)
public abstract class SpawnUtilFrameMixin {

    @WrapOperation(method = "trySpawnMob", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private static void sablemobpaths$enterWorld(ServerLevel level, Entity entity, Operation<Void> original) {
        SubLevel frame = Sable.HELPER.getContaining(level, entity.position());
        if (frame != null) {
            Vec3 world = frame.logicalPose().transformPosition(entity.position());
            entity.moveTo(world.x, world.y, world.z, entity.getYRot(), entity.getXRot());
        }
        original.call(level, entity);
    }
}
