# Clean-room record

Sable Mob Paths does a similar job to [Sable Pathfinder](https://modrinth.com/mod/sable-pathfinder)
by B34tingH34rt. That mod is All Rights Reserved, and none of its code is here.

## Rules for everyone working on this repo

- Never open the source of Sable Pathfinder or of `mmmpipi/sable-pathfinding-fix` (no licence), and
  never decompile either jar. That includes while debugging.
- Develop with Sable Pathfinder off the classpath.
- Loading its unmodified jar to compare behaviour, or to test that this mod switches off, is fine.

## What this mod was written from

| Input | Used for |
|---|---|
| Sable's source (PolyForm Shield 1.0.0) | How sub-level poses, plots and its existing pathing work |
| Minecraft and NeoForge | The pathing classes being hooked |
| Sable Pathfinder's public Modrinth description and changelogs | Which behaviours players expect |
| Black-box timings of mobs in-game | Measuring the gap and checking parity |

A short functional description of Sable Pathfinder also sits in Dungeon Train's source
(`compat/SablePathfinderPresence`). No code, no implementation detail.

The maintainer's own Claude session that wrote v1 opened only licence metadata and file names from
both repos, plus the commit messages on `mmmpipi`'s.

## Design origin

Villager support follows from vanilla's brain behaviours, which measure from a remembered block to a mob, and Sable's own `EatBlockGoalMixin`, which swaps a carried mob's `blockPosition()` for its plot position. Which scenarios needed to work came from running both mods side by side.

Searching in a sub-level's frame over a merged view of the world and nearby sub-levels follows
from how Sable's own `PathNavigationMixin` searches one frame, and from which blocks vanilla's
`WalkNodeEvaluator` reads.
