import React from "react"
import Hand from "../../components/kirves/Hand"
import { game, discardGame, extraCardGame, handSortingGame, handPowerGame } from "../data/games"
import { logAction } from "../logAction"

const action = logAction("selectCard")

// Match the live game's sizing container and hand spacing, without rendering a table.
const renderHand = handGame => (
  <div className="table-area">
    <div className="hand-area">
      <Hand game={handGame} action={action} />
    </div>
  </div>
)

export default {
  title: "Hand",
  group: "Components",
  variants: {
    playable: () => renderHand(game),
    sortedByPower: () => renderHand(handSortingGame),
    jokersAndJacks: () => renderHand(handPowerGame),
    beforeTrump: () => renderHand({ ...handSortingGame, trump: "", myAvailableActions: [] }),
    changedTrump: () => renderHand({ ...handSortingGame, trump: "SPADES" }),
    waitingForTurn: () => renderHand({ ...game, myAvailableActions: [] }),
    discard: () => renderHand(discardGame),
    extraCard: () => renderHand(extraCardGame),
    lastCard: () => renderHand({ ...game, myCardsInHand: ["QS"] }),
    empty: () => (
      <>
        <p>Ei kortteja kädessä.</p>
        {renderHand({ ...game, myCardsInHand: [], myExtraCard: null })}
      </>
    ),
  },
}
