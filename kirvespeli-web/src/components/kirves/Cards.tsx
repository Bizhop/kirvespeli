import type { CardCode, SuitName } from "../../types/kirves.ts"
import type React from "react"
import Card, { type CardProps } from "./Card.tsx"
import { sortHandCards } from "./handOrder.ts"

// Each card code is unique within a row, so it is the render identity. The backend index
// changes when earlier cards leave the row and is only passed on for actions.
// The slot wrapper owns layout movement; the card button keeps its own fan/hover transform.
const Cards = ({
  cards = [],
  handTrump,
  numOfPlayedRounds = 0,
  slotRef,
  playableIndexes,
  ...props
}: Omit<CardProps, "card" | "index"> & {
  cards?: readonly CardCode[]
  handTrump?: SuitName | null
  numOfPlayedRounds?: number
  slotRef?: (card: CardCode) => React.RefCallback<HTMLDivElement>
  playableIndexes?: readonly number[]
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
