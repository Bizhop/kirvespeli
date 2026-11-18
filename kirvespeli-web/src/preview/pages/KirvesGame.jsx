import React from "react"

import { KirvesGame } from "../../components/kirves/KirvesGame"
import { Header } from "../../components/shared/Header"
import {
  game,
  game2,
  dealerGame,
  waitingGame,
  waitingFourGame,
  speakingGame,
  suitGame,
} from "../data/games"
import { logAction } from "../logAction"

const baseData = {
  join: logAction("join"),
  action: logAction("action"),
  showAllCards: logAction("showAllCards"),
  authStatus: "authenticated",
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
    loading: () => <KirvesGame {...baseData} loading />,
    loadError: () => (
      <KirvesGame {...baseData} error="Yhteys palvelimeen epäonnistui" retry={logAction("retry")} />
    ),
    joinError: () => (
      <KirvesGame {...baseData} game={waitingGame} operationError="Peli on täynnä" />
    ),
    pendingAction: () => <KirvesGame {...baseData} game={speakingGame} pending />,
    waitingFourPlayers: page(waitingFourGame),
    gameStart: page(game),
    gameEnd: page(game2),
    speak: page(speakingGame),
    speakSuit: page(suitGame),
    lostGameChooseDealer: page(dealerGame),
  },
}
