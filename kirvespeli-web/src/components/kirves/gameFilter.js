export const GAME_FILTERS = [
  { value: "joinable", label: "Liityttävät", matches: game => Boolean(game.canJoin) },
  { value: "mine", label: "Omat pelit", matches: game => Boolean(game.joined) },
  { value: "all", label: "Kaikki", matches: () => true },
]

export const filterGames = (games, filter) => {
  const selected =
    GAME_FILTERS.find(f => f.value === filter) || GAME_FILTERS[GAME_FILTERS.length - 1]
  return games.filter(selected.matches)
}
