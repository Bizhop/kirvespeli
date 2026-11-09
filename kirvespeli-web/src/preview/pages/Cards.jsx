import React from "react"

import Cards from "../../components/kirves/Cards"
import { logAction } from "../logAction"

const cardsInHand = ["AD", "JC", "2H", "7H", "KS"]
const otherCardsInPlay = ["AC", "BJ", "3S", "9H", "QD"]

const action = logAction("selectCard")

export default {
  title: "Cards",
  group: "Components",
  variants: {
    myCards: () => <Cards scale={0.5} cards={cardsInHand} roundsWon={[]} action={action} />,
    myCardsWithSuit: () => (
      <Cards cards={cardsInHand} roundsWon={[]} action={action} firstCardSuit="HEARTS" />
    ),
    otherCards: () => (
      <Cards cards={otherCardsInPlay} roundsWon={[1, 3]} scale={2} numOfPlayedRounds={5} />
    ),
    otherCardsWithLastCard: () => (
      <Cards cards={otherCardsInPlay} roundsWon={[2, 4]} scale={2} numOfPlayedRounds={5} />
    ),
    otherCardsVisible: () => (
      <Cards
        cards={otherCardsInPlay}
        roundsWon={[2, 4]}
        scale={2}
        numOfPlayedRounds={5}
        cardsVisible
      />
    ),
  },
}
