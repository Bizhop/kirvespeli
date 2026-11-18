import type { Snapshot } from "../src/preview/data/kirves.ts"
import * as fixtures from "../src/preview/data/games.ts"

const isSnapshot = (value: unknown): value is Snapshot => {
  if (!value || typeof value !== "object") return false
  return (
    "players" in value &&
    Array.isArray(value.players) &&
    "myAvailableActions" in value &&
    Array.isArray(value.myAvailableActions) &&
    "scores" in value &&
    Array.isArray(value.scores)
  )
}

// Include directly exported snapshots and keyed table/peek variants, excluding the mutable engine.
export const previewSnapshots: [string, Snapshot][] = Object.entries(fixtures).flatMap(
  ([name, value]) => {
    if (isSnapshot(value)) return [[name, value]]
    if (!value || typeof value !== "object") return []
    return Object.entries(value).flatMap(([key, game]) =>
      isSnapshot(game) ? [[`${name}[${key}]`, game] as [string, Snapshot]] : [],
    )
  },
)
