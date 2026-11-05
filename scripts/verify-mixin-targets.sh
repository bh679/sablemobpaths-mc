#!/usr/bin/env bash
# Checks the Sable internals this mod calls still exist in a Sable jar. Vanilla targets are covered
# by compilation and by "required": true in the mixin config.
# Usage: scripts/verify-mixin-targets.sh <sable-jar>
set -euo pipefail
JAR="${1:?usage: verify-mixin-targets.sh <sable-jar>}"
WORK="$(mktemp -d)"
trap 'rm -r "$WORK"' EXIT
unzip -qo "$JAR" -d "$WORK"
check() {
  local class="$1" member="$2"
  if ! javap -cp "$WORK" -public "$class" 2>/dev/null | grep -F "$member" >/dev/null; then
    echo "MISSING: $class :: $member" >&2
    exit 1
  fi
  echo "ok: $class :: $member"
}
MOVE=dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension
check "$MOVE" 'sable$setTrackingSubLevel'
check "$MOVE" 'sable$getLastTrackingSubLevelID'
check "$MOVE" 'sable$setLastTrackingSubLevelID'
check dev.ryanhcode.sable.mixinterface.entity.pathfinding.PathExtension 'sable$setLocalPath'
# Sable's start-node hook must still read the mob through its tracking sub-level.
javap -p -c -cp "$WORK" dev.ryanhcode.sable.mixin.entity.entity_pathfinding.WalkNodeEvaluatorMixin \
  | grep -F 'getTrackingSubLevel' >/dev/null || { echo "MISSING: WalkNodeEvaluatorMixin no longer reads the tracking sub-level" >&2; exit 1; }
echo "ok: WalkNodeEvaluatorMixin reads the tracking sub-level"
