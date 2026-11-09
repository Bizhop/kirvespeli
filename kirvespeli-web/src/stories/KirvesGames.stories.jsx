import React from "react"

import KirvesGames from "../components/kirves/KirvesGames"
import { games } from "./data/games"
import { user, other } from "./data/users"

const getLog = params => alert("Get log, params: " + JSON.stringify(params))

const deleteGame = id => alert("Delete game id: " + id)

export default {
  title: "Components/KirvesGames",
  component: KirvesGames,
}

export const asAdmin = () => (
  <KirvesGames games={games} user={user} getLog={getLog} deleteGame={deleteGame} />
)
export const asUser = () => <KirvesGames games={games} user={other} />
