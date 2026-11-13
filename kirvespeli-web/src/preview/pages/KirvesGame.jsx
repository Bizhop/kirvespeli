import React from "react"

import { KirvesGame } from "../../components/kirves/KirvesGame"
import { Header } from "../../components/shared/Header"
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

// The site header is included so the fit-to-viewport layout is checked as on the live page.
const page = gameData => () => (
  <>
    <Header loggedIn={true} logout={logAction("logout")} />
    <KirvesGame {...baseData} game={gameData} />
  </>
)

export default {
  title: "KirvesGame",
  group: "Containers",
  variants: {
    waitingForPlayers: page(waitingGame),
    gameStart: page(game),
    gameEnd: page(game2),
    speak: page(speakingGame),
    speakSuit: page(suitGame),
    lostGameChooseDealer: page(dealerGame),
  },
}
