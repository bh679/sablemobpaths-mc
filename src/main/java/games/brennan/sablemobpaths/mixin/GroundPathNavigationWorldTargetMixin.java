package games.brennan.sablemobpaths.mixin;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import games.brennan.sablemobpaths.CrossFrameSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A world position on a sub-level's deck reads as air in the world, and vanilla answers that by
 * scanning down to the ground under it. Turn it into plot coordinates first, which ends the
 * recursion here because a plot position isn't asked about again.
 */
@Mixin(GroundPathNavigation.class)
public abstract class GroundPathNavigationWorldTargetMixin extends PathNavigation {

    private GroundPathNavigationWorldTargetMixin(Mob mob, Level level) {
        super(mob, level);
    }

    @Shadow public abstract Path createPath(BlockPos pos, int accuracy);

    @Inject(method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",
        at = @At("HEAD"), cancellable = true)
    private void sablemobpaths$worldTargetOnDeck(BlockPos pos, int accuracy, CallbackInfoReturnable<Path> cir) {
        if (level.isClientSide || Sable.HELPER.getContaining(level, pos) != null) return;
        SubLevel frame = CrossFrameSearch.standingOn(level, pos);
        if (frame != null) {
            cir.setReturnValue(createPath(BlockPos.containing(
                frame.logicalPose().transformPositionInverse(pos.getCenter())), accuracy));
        }
    }
}
