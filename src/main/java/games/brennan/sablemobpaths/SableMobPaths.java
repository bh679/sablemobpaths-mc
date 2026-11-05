package games.brennan.sablemobpaths;

import net.neoforged.fml.common.Mod;

/**
 * Sable Mob Paths: ground mobs path between Sable sub-levels and the world.
 *
 * <p>Sable already paths a mob around the one sub-level it or its target is on. A search that
 * has to change frame (ground to deck, deck to ground, deck to next deck) finds nothing, because
 * each frame's blocks are invisible from the other. {@link FrameRegion} gives the search one
 * merged view of all of them.</p>
 *
 * <p>Off when Sable Pathfinder is installed (see {@link MobPathsMixinPlugin}). No config, no
 * commands, no registry content.</p>
 */
@Mod(SableMobPaths.MOD_ID)
public final class SableMobPaths {

    public static final String MOD_ID = "sablemobpaths";

    public SableMobPaths() {
        // Nothing to register: the mixin config declared in neoforge.mods.toml is the mod.
    }
}
