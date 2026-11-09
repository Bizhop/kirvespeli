import React from "react"

import { KirvesGame } from "../components/kirves/KirvesGame"
import { game, game2 } from "./data/games"
import { user } from "./data/users"

const baseData = {
  join: data => alert("Join, data: " + JSON.stringify(data)),
  refresh: data => alert("Refresh, data: " + JSON.stringify(data)),
  action: data => alert("Action, data: " + JSON.stringify(data)),
  showAllCards: () => alert("Show all cards"),
  getGame: () => {},
  user,
  socketDisabled: true,
}

const gameSpeak = {
  ...game,
  myAvailableActions: ["SPEAK"],
  players: [
    {
      ...game.players[0],
      availableActions: ["SPEAK"],
    },
  ],
}

const gameSpeakSuit = {
  ...game,
  myAvailableActions: ["SPEAK_SUIT"],
  players: [
    {
      ...game.players[0],
      availableActions: ["SPEAK_SUIT"],
    },
  ],
}

export default {
  title: "Containers/KirvesGame",
  component: KirvesGame,
}

export const gameStart = () => <KirvesGame {...baseData} game={game} />
export const gameEnd = () => <KirvesGame {...baseData} game={game2} />
export const speak = () => <KirvesGame {...baseData} game={gameSpeak} />
export const speakSuit = () => <KirvesGame {...baseData} game={gameSpeakSuit} />
