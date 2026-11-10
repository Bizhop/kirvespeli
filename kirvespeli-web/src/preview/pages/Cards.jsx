import React from "react"

import Cards from "../../components/kirves/Cards"
import { handStartGame, handEndGame } from "../data/games"
import { logAction } from "../logAction"

const action = logAction("selectCard")
// rows after the three-player hand: Ville took tricks 1, 2 and 4, Terhi the last one
const [ville, , terhi] = handEndGame.players
const row = (player, extra = {}) => (
  <Cards
    cards={player.playedCards}
    roundsWon={player.roundsWon}
    numOfPlayedRounds={handEndGame.numOfPlayedRounds}
    {...extra}
  />
)

export default {
  title: "Cards",
  group: "Components",
  variants: {
    myCards: () => (
      <Cards
        cards={handStartGame.myCardsInHand}
        roundsWon={[]}
        action={action}
        actionName="PLAY_CARD"
      />
    ),
    myCardsWithSuit: () => (
      <Cards
        cards={handStartGame.myCardsInHand}
        handTrump={handStartGame.trump}
        roundsWon={[]}
        action={action}
        actionName="PLAY_CARD"
      />
    ),
    otherCards: () => row(ville),
    otherCardsWithLastCard: () => row(terhi),
    otherCardsVisible: () => row(ville, { cardsVisible: true }),
  },
}
