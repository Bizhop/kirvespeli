import React from "react"

import { KirvesGame } from "../../components/kirves/KirvesGame"
import { game, game2 } from "../data/games"
import { user } from "../data/users"
import { logAction } from "../logAction"

const baseData = {
  join: logAction("join"),
  refresh: logAction("refresh"),
  action: logAction("action"),
  showAllCards: logAction("showAllCards"),
  getGame: () => {},
  user,
  socketDisabled: true,
}

const withMyActions = actions => ({
  ...game,
  myAvailableActions: actions,
  players: [{ ...game.players[0], availableActions: actions }],
})

export default {
  title: "KirvesGame",
  group: "Containers",
  variants: {
    gameStart: () => <KirvesGame {...baseData} game={game} />,
    gameEnd: () => <KirvesGame {...baseData} game={game2} />,
    speak: () => <KirvesGame {...baseData} game={withMyActions(["SPEAK"])} />,
    speakSuit: () => <KirvesGame {...baseData} game={withMyActions(["SPEAK_SUIT"])} />,
  },
}
