import React from "react"
import RenderGame from "../../components/kirves/RenderGame"
import {
  tableGames,
  waitingGame,
  dealerGame,
  cutGame,
  cutTurnGame,
  speakingGame,
  suitGame,
  spectatorGame,
  extraCardGame,
  discardGame,
  departureGame,
  departureWaitingGame,
  departureAdminGame,
  departureMultipleGame,
  departureAcknowledgedGame,
  departureEarlyEndGame,
  departureGameEndGame,
} from "../data/games"
import { logAction } from "../logAction"
const props = {
  action: logAction("action"),
  join: logAction("join"),
  showAllCards: logAction("showAllCards"),
}
export default {
  title: "Table",
  group: "Containers",
  variants: {
    ...Object.fromEntries(
      Object.entries(tableGames).map(([count, game]) => [
        `${count} players`,
        () => <RenderGame game={game} {...props} />,
      ]),
    ),
    waiting: () => <RenderGame game={waitingGame} {...props} />,
    dealerSelection: () => <RenderGame game={dealerGame} {...props} />,
    cutTurn: () => <RenderGame game={cutTurnGame} {...props} />,
    cut: () => <RenderGame game={cutGame} {...props} />,
    speaking: () => <RenderGame game={speakingGame} {...props} />,
    suitSelection: () => <RenderGame game={suitGame} {...props} />,
    extraCard: () => <RenderGame game={extraCardGame} {...props} />,
    discard: () => <RenderGame game={discardGame} {...props} />,
    spectator: () => <RenderGame game={spectatorGame} {...props} />,
    departure: () => <RenderGame game={departureGame} {...props} />,
    departureWaiting: () => <RenderGame game={departureWaitingGame} {...props} />,
    departureAdmin: () => <RenderGame game={departureAdminGame} {...props} />,
    departureMultiple: () => <RenderGame game={departureMultipleGame} {...props} />,
    departureAcknowledged: () => <RenderGame game={departureAcknowledgedGame} {...props} />,
    departureEarlyEnd: () => <RenderGame game={departureEarlyEndGame} {...props} />,
    departureGameEnd: () => <RenderGame game={departureGameEndGame} {...props} />,
    replay: () => <RenderGame game={tableGames[6]} isReplay cardsVisible />,
  },
}
