package games.brennan.sablemobpaths.mixin;

import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Beds and bells on a sub-level make a village where the sub-level is, not only at its plot. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelVillageMixin {

    @Inject(method = "sectionsToVillage(Lnet/minecraft/core/SectionPos;)I", at = @At("RETURN"), cancellable = true)
    private void sablemobpaths$includeSubLevels(SectionPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(VillagerFrame.sectionsToVillage((ServerLevel) (Object) this, pos, cir.getReturnValue()));
    }
}
