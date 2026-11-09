import React from "react"

import Cards from "../components/kirves/Cards"

const cardsInHand = ["AD", "JC", "2H", "7H", "KS"]
const otherCardsInPlay = ["AC", "BJ", "3S", "9H", "QD"]

const action = data => alert("Selected card index: " + data.index)

export default {
  title: "Components/Cards",
  component: Cards,
}

export const myCards = () => <Cards scale={0.5} cards={cardsInHand} roundsWon={[]} action={action} />
export const myCardsWithSuit = () => (
  <Cards cards={cardsInHand} roundsWon={[]} action={action} firstCardSuit="HEARTS" />
)
export const otherCards = () => (
  <Cards cards={otherCardsInPlay} roundsWon={[1, 3]} scale={2} numOfPlayedRounds={5} />
)
export const otherCardsWithLastCard = () => (
  <Cards cards={otherCardsInPlay} roundsWon={[2, 4]} scale={2} numOfPlayedRounds={5} />
)
export const otherCardsVisible = () => (
  <Cards cards={otherCardsInPlay} roundsWon={[2, 4]} scale={2} numOfPlayedRounds={5} cardsVisible />
)
