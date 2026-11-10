import React from "react"

import { KirvesContainer } from "../../components/kirves/KirvesContainer"
import { user } from "../data/users"
import { games, hand } from "../data/games"
import { logId, logItems } from "../data/logs"
import { logAction } from "../logAction"

const baseData = {
  user,
  games,
  logItems,
  logId,
  init: logAction("init"),
  deleteGame: logAction("deleteGame"),
  getLog: logAction("getLog"),
  getReplay: logAction("getReplay"),
  restoreGame: logAction("restoreGame"),
  getGames: logAction("getGames"),
  gamesFetched: true,
}

export default {
  title: "Kirves",
  group: "Containers",
  variants: {
    initial: () => <KirvesContainer {...baseData} />,
    error: () => <KirvesContainer {...baseData} error="Nickname on jo käytössä" />,
    withLog: () => <KirvesContainer {...baseData} logVisible selectedLogIndex={0} />,
    withLogAndReplay: () => (
      <KirvesContainer {...baseData} logVisible selectedLogIndex={2} replay={hand.replays[2]} />
    ),
  },
}
