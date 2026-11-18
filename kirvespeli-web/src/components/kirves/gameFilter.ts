import type { GameBrief } from "../../types/kirves.ts"
export const GAME_FILTERS = [
  {
    value: "joinable",
    label: "Liityttävät",
    matches: (game: Pick<GameBrief, "canJoin" | "joined">) => Boolean(game.canJoin),
  },
  {
    value: "mine",
    label: "Omat pelit",
    matches: (game: Pick<GameBrief, "canJoin" | "joined">) => Boolean(game.joined),
  },
  { value: "all", label: "Kaikki", matches: () => true },
]

export const filterGames = <T extends Pick<GameBrief, "canJoin" | "joined">>(
  games: readonly T[],
  filter: string,
) => {
  const selected =
    GAME_FILTERS.find(f => f.value === filter) || GAME_FILTERS[GAME_FILTERS.length - 1]
  return games.filter(selected.matches)
}
