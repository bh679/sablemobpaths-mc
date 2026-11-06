package games.brennan.sablemobpaths.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Path.class)
public interface PathTargetAccessor {

    @Mutable
    @Accessor("target")
    void sablemobpaths$setTarget(BlockPos target);
}
