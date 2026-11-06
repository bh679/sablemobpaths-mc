# Product Engineer: Sable Mob Paths

<!-- Source: github.com/bh679/claude-templates/templates/engineering/product/CLAUDE.md (adapted for a NeoForge sibling mod) -->

## Project Overview

- **Project:** Sable Mob Paths. Ground mobs path across Sable sub-level boundaries. Sibling of Dungeon Train (`bh679/dungeon-train-mc`), bundled inside it via jarJar.
- **Mod Loader:** NeoForge 1.21.1, Java 21. NeoForge-only because Sable is.
- **Key Dependency:** Sable, minimum `[2.0.5,)`, never bundled (PolyForm Shield).
- **GitHub Project:** https://github.com/users/bh679/projects/16 (shared with Dungeon Train)

## Clean room (read first)

Read [CLEANROOM.md](CLEANROOM.md). **Never open Sable Pathfinder's or `mmmpipi/sable-pathfinding-fix`'s code, and never decompile either jar, even to debug.** Sable's own source is fine.

## How it works

- `PathNavigationCrossFrameMixin` runs ahead of Sable's hook on `createPath` and takes only searches that change frame (`PathRoute`). Same-frame searches stay with Sable or vanilla.
- `CrossFrameSearch` searches in the destination's sub-level frame, else the mob's carrier's, so the stretch that matters stays put as it moves. It points the mob's tracking sub-level at that frame for the search, because Sable's start-node hooks read the mob through it.
- `GroundPathNavigationWorldTargetMixin` turns a world target on a deck into plot coordinates before vanilla scans down to the ground under it.
- A world-to-world search with a sub-level between the two ends (`CrossFrameSearch.inTheWay`) also goes through the merged view, so the mob crosses or rounds it. Out of budget, it falls back to vanilla.
- `FrameRegion` is the merged view: the frame's block, else the world's, else another sub-level's. Never loads chunks.
- `PathfindingContextFrameMixin` keeps merged answers out of the level-wide path-type cache.
- `MobPathsMixinPlugin` skips every mixin when `sable_pathfinder` is loaded.
- Searches are throttled per mob and per tick (`PathRoute`).

## Standards and gates

Follows `bh679/claude-templates` rules. All changes go through Gate 1 (plan), Gate 2 (test), Gate 3 (merge), per `.claude/gates/`.

- **Gate 2:** `./gradlew build`, `scripts/verify-mixin-targets.sh <sable jar>`, then test on a **moving** Dungeon Train with Sable Pathfinder off the classpath.
- **Gate 3:** PR, green CI, squash-merge after approval, then a manual MINOR bump of `mod_version` on main.
- **Release:** `gh workflow run release.yml -f tag=v<version>`. Never `git tag` by hand. Then raise `sablemobpaths_version` in Dungeon Train.

## Build

```bash
./gradlew build
./gradlew test
```

Gradle needs JDK 21: `JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.11+10/Contents/Home`.
