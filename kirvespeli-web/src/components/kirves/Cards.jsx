import React from "react"
import Card from "./Card"
import { sortHandCards } from "./handOrder"

const Cards = ({ cards = [], handTrump, numOfPlayedRounds, ...props }) => (
  <div className="cards">
    {sortHandCards(cards, handTrump).map(({ card, index }) => (
      <Card
        key={`${index}:${card}`}
        {...props}
        card={card}
        index={index}
        lastCard={numOfPlayedRounds - 1 === index}
      />
    ))}
  </div>
)
export default Cards
