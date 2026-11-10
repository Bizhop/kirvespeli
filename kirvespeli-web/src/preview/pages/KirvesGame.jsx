import React from "react"

import { KirvesGame } from "../../components/kirves/KirvesGame"
import { game, game2, dealerGame, waitingGame, speakingGame, suitGame } from "../data/games"
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

export default {
  title: "KirvesGame",
  group: "Containers",
  variants: {
    waitingForPlayers: () => <KirvesGame {...baseData} game={waitingGame} />,
    gameStart: () => <KirvesGame {...baseData} game={game} />,
    gameEnd: () => <KirvesGame {...baseData} game={game2} />,
    speak: () => <KirvesGame {...baseData} game={speakingGame} />,
    speakSuit: () => <KirvesGame {...baseData} game={suitGame} />,
    lostGameChooseDealer: () => <KirvesGame {...baseData} game={dealerGame} />,
  },
}
