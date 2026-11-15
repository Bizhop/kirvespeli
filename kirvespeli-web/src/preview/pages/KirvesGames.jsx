import React from "react"

import KirvesGames from "../../components/kirves/KirvesGames"
import { games, peekGames } from "../data/games"
import { user, other } from "../data/users"
import { logAction } from "../logAction"

// Peeking game 16 fails, to show the error in the dialog.
const fetchGame = gameId => {
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
    asUser: () => <KirvesGames games={games} user={other} fetchGame={fetchGame} />,
  },
}
