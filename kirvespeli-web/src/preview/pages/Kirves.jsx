import React from "react"

import { KirvesContainer } from "../../components/kirves/KirvesContainer"
import { user } from "../data/users"
import { games, hand } from "../data/games"
import { logId, logItems } from "../data/logs"
import { logAction } from "../logAction"

const baseData = {
  user,
  authStatus: "authenticated",
  games,
  logItems,
  logId,
  init: logAction("init"),
  deleteGame: logAction("deleteGame"),
  getLog: logAction("getLog"),
  getReplay: logAction("getReplay"),
  restoreGame: logAction("restoreGame"),
  getGames: logAction("getGames"),
}

export default {
  title: "Kirves",
  group: "Containers",
  variants: {
    initial: () => <KirvesContainer {...baseData} />,
    loading: () => <KirvesContainer {...baseData} games={[]} loading />,
    loadError: () => (
      <KirvesContainer
        {...baseData}
        games={[]}
        error="Yhteys palvelimeen epäonnistui"
        retry={logAction("retry")}
      />
    ),
    pending: () => <KirvesContainer {...baseData} pending />,
    operationError: () => <KirvesContainer {...baseData} operationError="Et voi poistaa peliä" />,
    restored: () => <KirvesContainer {...baseData} notice="Pelitilanne palautettu" />,
    withLog: () => <KirvesContainer {...baseData} logVisible selectedLogIndex={0} />,
    withLogAndReplay: () => (
      <KirvesContainer {...baseData} logVisible selectedLogIndex={2} replay={hand.replays[2]} />
    ),
  },
}
