package games.brennan.sablemobpaths.mixin;

import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Holds the level and mob for the length of a brain tick. See {@link VillagerFrame}. */
@Mixin(Brain.class)
public abstract class BrainTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void sablemobpaths$enter(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        VillagerFrame.enter(level, entity);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void sablemobpaths$exit(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        VillagerFrame.exit();
    }
}
