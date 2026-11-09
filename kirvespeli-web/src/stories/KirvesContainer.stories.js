import React from "react"

import { KirvesContainer } from "../components/kirves/KirvesContainer"
import { user } from "./data/users"
import { games, game } from "./data/games"
import { logId, logItems } from "./data/logs"

const baseData = {
  user,
  games,
  logItems,
  logId,
  init: () => alert("New game"),
  deleteGame: data => alert("Delete game, data: " + JSON.stringify(data)),
  getLog: data => alert("Get log, data: " + JSON.stringify(data)),
  getReplay: data => alert("Get replay, data: " + JSON.stringify(data)),
  restoreGame: data => alert("Restore game, data: " + JSON.stringify(data)),
  getGames: () => alert("Get games"),
  gamesFetched: true,
}

export default {
  title: "Containers/Kirves",
}

export const initial = () => <KirvesContainer {...baseData} />
export const error = () => <KirvesContainer {...baseData} error="Some random error!" />
export const withLog = () => <KirvesContainer {...baseData} logVisible selectedLogIndex={0} />
export const withLogAndReplay = () => (
  <KirvesContainer {...baseData} logVisible selectedLogIndex={2} replay={game} />
)
