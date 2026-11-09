import React from "react"

import KirvesGames from "../../components/kirves/KirvesGames"
import { games } from "../data/games"
import { user, other } from "../data/users"
import { logAction } from "../logAction"

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
      />
    ),
    asUser: () => <KirvesGames games={games} user={other} />,
  },
}
