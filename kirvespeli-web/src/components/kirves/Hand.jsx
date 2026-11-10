import React from "react"
import Cards from "./Cards"
import { SvgImage } from "../shared/images"
const Hand = ({ game, action }) => {
  const actions = game.myAvailableActions ?? []
  const actionName = actions.includes("DISCARD")
    ? "DISCARD"
    : actions.includes("PLAY_CARD")
      ? "PLAY_CARD"
      : undefined
  if (!game.myCardsInHand?.length && !game.myExtraCard) return null
  return (
    <div className="own-hand" aria-label="Omat kortit">
      <Cards
        cards={game.myCardsInHand}
        handTrump={game.trump}
        roundsWon={[]}
        action={action}
        actionName={actionName}
        gameId={game.id}
      />
      {game.myExtraCard && (
        <div className="hand-extra">
          <span>Ylimääräinen</span>
          <SvgImage name={game.myExtraCard} className="card-image" />
        </div>
      )}
    </div>
  )
}
export default Hand
