import type { PreviewPage } from "../types.ts"

import KirvesGames from "../../components/kirves/KirvesGames.tsx"
import { games, peekGames } from "../data/games.ts"
import { user, other } from "../data/users.ts"
import { logAction } from "../logAction.ts"

// Peeking game 16 fails, to show the error in the dialog.
const fetchGame = (gameId: number) => {
  logAction("fetchGame")(gameId)
  const game = peekGames[gameId]
  return game ? Promise.resolve(game) : Promise.reject(new Error("not found"))
}

export default {
  title: "KirvesGames",
  group: "Components",
  variants: {
    asAdmin: () => (
      <KirvesGames
        games={games}
        user={user}
        getLog={logAction("getLog")}
        deleteGame={logAction("deleteGame")}
        fetchGame={fetchGame}
      />
    ),
    asUser: () => (
      <KirvesGames
        games={games}
        user={other}
        getLog={logAction("getLog")}
        deleteGame={logAction("deleteGame")}
        fetchGame={fetchGame}
      />
    ),
  },
} satisfies PreviewPage
