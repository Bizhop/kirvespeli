import React from "react"
import Cards from "./Cards"
import { SvgImage } from "../shared/images"
import { useMotionRef } from "./CardMotion"
import { ownKey } from "./cardMotion"
import { playableCardIndexes } from "./playableCards"
const Hand = ({ game, action }) => {
  const motionRef = useMotionRef()
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
        playableIndexes={
          actionName === "PLAY_CARD"
            ? playableCardIndexes(game.myCardsInHand, game.firstCardSuit, game.trump)
            : undefined
        }
        gameId={game.id}
        slotRef={motionRef && (card => motionRef(ownKey(card)))}
      />
      {game.myExtraCard && (
        <div className="hand-extra">
          <span>Ylimääräinen</span>
          <SvgImage
            name={game.myExtraCard}
            className="card-image"
            ref={motionRef?.(ownKey(game.myExtraCard))}
          />
        </div>
      )}
    </div>
  )
}
export default Hand
