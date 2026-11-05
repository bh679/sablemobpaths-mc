package games.brennan.sablemobpaths.mixin;

import games.brennan.sablemobpaths.FrameRegion;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The level-wide path-type cache is keyed by position alone and only cleared when that block
 * changes. A merged view's answer depends on where a sub-level is right now, so storing it there
 * would poison every later search at that position.
 */
@Mixin(PathfindingContext.class)
public abstract class PathfindingContextFrameMixin {

    @Shadow @Final @Mutable @Nullable private PathTypeCache cache;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sablemobpaths$skipSharedCache(CollisionGetter level, Mob mob, CallbackInfo ci) {
        if (level instanceof FrameRegion) cache = null;
    }
}
