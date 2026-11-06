package games.brennan.sablemobpaths.mixin;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A raid started on a moving sub-level keeps its centre on it. The centre stays in world coordinates
 * and all other raid code stays vanilla. Not saved: after a reload the centre stays put, and the raid
 * ends once that spot stops being a village.
 */
@Mixin(Raid.class)
public abstract class RaidCarriageMixin {

    @Shadow private BlockPos center;

    @Unique @Nullable private SubLevel sablemobpaths$carrier;
    @Unique @Nullable private Vec3 sablemobpaths$localCenter;

    @Inject(method = "absorbRaidOmen", at = @At("HEAD"))
    private void sablemobpaths$anchor(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (sablemobpaths$carrier != null) return;
        SubLevel carrier = Sable.HELPER.getTrackingSubLevel(player);
        if (carrier == null) return;
        sablemobpaths$carrier = carrier;
        sablemobpaths$localCenter = carrier.logicalPose().transformPositionInverse(Vec3.atCenterOf(center));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sablemobpaths$followCarrier(CallbackInfo ci) {
        if (sablemobpaths$carrier == null) return;
        if (sablemobpaths$carrier.isRemoved()) {
            sablemobpaths$carrier = null;
            return;
        }
        center = BlockPos.containing(sablemobpaths$carrier.logicalPose().transformPosition(sablemobpaths$localCenter));
    }
}
