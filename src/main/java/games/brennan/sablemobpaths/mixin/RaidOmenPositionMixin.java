package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The omen's position is 30 seconds old when it fires, far behind a player on a moving sub-level. */
@Mixin(targets = "net.minecraft.world.effect.RaidOmenMobEffect")
public abstract class RaidOmenPositionMixin {

    @ModifyExpressionValue(method = "applyEffectTick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;getRaidOmenPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos sablemobpaths$whereThePlayerIsNow(@Nullable BlockPos omen, @Local ServerPlayer player) {
        return omen != null && Sable.HELPER.getTrackingSubLevel(player) != null ? player.blockPosition() : omen;
    }
}
