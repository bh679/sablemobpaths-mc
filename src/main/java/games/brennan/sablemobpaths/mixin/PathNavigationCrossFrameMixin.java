package games.brennan.sablemobpaths.mixin;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinterface.entity.pathfinding.PathExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import games.brennan.sablemobpaths.CrossFrameSearch;
import games.brennan.sablemobpaths.PathRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Takes over only the searches that change frame. Priority below the default so this runs ahead of
 * Sable's own hook on the same method, which handles the rest.
 */
@Mixin(value = PathNavigation.class, priority = 900)
public abstract class PathNavigationCrossFrameMixin {

    @Shadow @Final protected Mob mob;
    @Shadow @Final protected Level level;
    @Shadow @Nullable protected Path path;
    @Shadow @Nullable private BlockPos targetPos;
    @Shadow private int reachRange;
    @Shadow @Final private PathFinder pathFinder;
    @Shadow private float maxVisitedNodesMultiplier;
    @Shadow protected NodeEvaluator nodeEvaluator;

    @Shadow protected abstract boolean canUpdatePath();
    @Shadow protected abstract void resetStuckTimeout();
    @Shadow public abstract void recomputePath();

    @Unique private long sablemobpaths$lastSearch = Long.MIN_VALUE / 2;
    @Unique @Nullable private SubLevel sablemobpaths$lastCarrier;

    @Inject(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;",
        at = @At("HEAD"), cancellable = true)
    private void sablemobpaths$createPath(Set<BlockPos> targets, int range, boolean fromAbove, int reach,
                                          float maxDistance, CallbackInfoReturnable<Path> cir) {
        if (level.isClientSide || targets.isEmpty() || !(nodeEvaluator instanceof WalkNodeEvaluator)) return;

        SubLevel carrier = Sable.HELPER.getTrackingSubLevel(mob);
        SubLevel frame = CrossFrameSearch.destinationFrame(level, targets);
        boolean inside = frame == null && CrossFrameSearch.insideMobFrame(carrier, targets);
        SubLevel inTheWay = null;
        if (!PathRoute.crossesFrames(carrier, frame, inside)) {
            if (carrier != null || frame != null) return;
            inTheWay = CrossFrameSearch.inTheWay(level, mob, targets);
            if (inTheWay == null) return;
        }

        if (!canUpdatePath()) {
            cir.setReturnValue(null);
            return;
        }
        if (path != null && !path.isDone() && targets.contains(targetPos)) {
            cir.setReturnValue(path);
            return;
        }
        long now = level.getGameTime();
        if (!CrossFrameSearch.trySpend(now, sablemobpaths$lastSearch)) {
            // World to world still has vanilla's answer; only a true crossing has to wait.
            if (inTheWay == null) cir.setReturnValue(path != null && !path.isDone() ? path : null);
            return;
        }
        sablemobpaths$lastSearch = now;

        SubLevel searchFrame = frame != null ? frame : carrier != null ? carrier : inTheWay;
        Path found = CrossFrameSearch.search(level, mob, pathFinder, targets, searchFrame, fromAbove,
            (int) (maxDistance + range), maxDistance, reach, maxVisitedNodesMultiplier);
        if (found != null && found.getTarget() != null) {
            // A world destination stays in world coordinates: a later recompute may start off the deck.
            targetPos = frame != null ? found.getTarget() : targets.iterator().next();
            reachRange = reach;
            resetStuckTimeout();
            ((PathExtension) (Object) found).sable$setLocalPath(level, true);
        }
        cir.setReturnValue(found);
    }

    /** Stepping onto or off a sub-level changes which search applies, so ask again. */
    @Inject(method = "tick", at = @At("HEAD"))
    private void sablemobpaths$repathOnCarrierChange(CallbackInfo ci) {
        if (level.isClientSide) return;
        SubLevel carrier = Sable.HELPER.getTrackingSubLevel(mob);
        if (carrier == sablemobpaths$lastCarrier) return;
        sablemobpaths$lastCarrier = carrier;
        if (path != null && !path.isDone() && targetPos != null) recomputePath();
    }
}
