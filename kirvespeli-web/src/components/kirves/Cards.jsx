import React from "react"
import Card from "./Card"
import { sortHandCards } from "./handOrder"

// Each card code is unique within a row, so it is the render identity. The backend index
// changes when earlier cards leave the row and is only passed on for actions.
// The slot wrapper owns layout movement; the card button keeps its own fan/hover transform.
const Cards = ({
  cards = [],
  handTrump,
  numOfPlayedRounds,
  slotRef,
  playableIndexes,
  ...props
}) => (
  <div className="cards">
    {sortHandCards(cards, handTrump).map(({ card, index }) => (
      <div key={card} className="card-slot" ref={slotRef?.(card)}>
        <Card
          {...props}
          card={card}
          index={index}
          lastCard={numOfPlayedRounds - 1 === index}
          legal={playableIndexes == null || playableIndexes.includes(index)}
        />
      </div>
    ))}
  </div>
)
export default Cards
