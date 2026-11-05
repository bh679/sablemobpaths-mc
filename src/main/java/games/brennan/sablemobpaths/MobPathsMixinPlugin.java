package games.brennan.sablemobpaths;

import net.neoforged.fml.loading.LoadingModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Skips every mixin when Sable Pathfinder is installed, so the two never patch the same search.
 * One line in {@code latest.log} says which one a player is running.
 */
public final class MobPathsMixinPlugin implements IMixinConfigPlugin {

    public static final String TAG = "[SableMobPaths]";

    /** Mod id Sable Pathfinder declares. Only ever compared as a string. */
    private static final String YIELD_TO = "sable_pathfinder";

    // log4j directly, not slf4j via LogUtils: this runs during early class transformation, before
    // the game's logging facade is a safe thing to touch.
    private static final Logger LOGGER = LogManager.getLogger(SableMobPaths.MOD_ID);

    private boolean active;

    @Override
    public void onLoad(String mixinPackage) {
        active = LoadingModList.get().getModFileById(YIELD_TO) == null;
        LOGGER.info("{} {}", TAG, active
            ? "active: mobs path across sub-level boundaries"
            : "off: Sable Pathfinder is installed");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return active;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                          IMixinInfo mixinInfo) {
        LOGGER.info("{} mixin applied: {} -> {}", TAG, mixinClassName, targetClassName);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                         IMixinInfo mixinInfo) {
    }
}
