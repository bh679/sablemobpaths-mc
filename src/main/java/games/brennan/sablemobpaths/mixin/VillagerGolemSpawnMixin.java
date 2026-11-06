package games.brennan.sablemobpaths.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import games.brennan.sablemobpaths.VillagerFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A carried villager looks for a golem's spawn spot on its deck, not on the ground under it. */
@Mixin(Villager.class)
public abstract class VillagerGolemSpawnMixin {

    @ModifyExpressionValue(method = "spawnGolemIfNeeded", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/npc/Villager;blockPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos sablemobpaths$spawnFromDeck(BlockPos world) {
        return VillagerFrame.inCarrierFrame(world);
    }
}
