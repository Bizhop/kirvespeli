import React from "react"
import Card from "./Card"
import { sortHandCards } from "./handOrder"
import { SvgImage } from "../shared/images"

const Cards = ({ cards = [], handTrump, numOfPlayedRounds, firstCardSuit, ...props }) => (
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
    {firstCardSuit && <SvgImage name={`Suit${firstCardSuit}`} className="suit-indicator" />}
  </div>
)
export default Cards
