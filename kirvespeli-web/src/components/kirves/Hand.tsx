import type { GameOut } from "../../types/kirves.ts"
import type { GameActionHandler } from "./props.ts"
import Cards from "./Cards.tsx"
import { SvgImage } from "../shared/images.tsx"
import { useMotionRef } from "./CardMotion.tsx"
import { ownKey } from "./cardMotion.ts"
import { playableCardIndexes } from "./playableCards.ts"
const Hand = ({ game, action }: { game: GameOut; action?: GameActionHandler }) => {
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
        cards={game.myCardsInHand ?? []}
        handTrump={game.trump}
        roundsWon={[]}
        action={action}
        actionName={actionName}
        playableIndexes={
          actionName === "PLAY_CARD"
            ? playableCardIndexes(game.myCardsInHand ?? [], game.firstCardSuit, game.trump)
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
