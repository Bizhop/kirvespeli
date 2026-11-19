import type { PlayerOut } from "../../types/kirves.ts"
import type { PreviewPage } from "../types.ts"
import type React from "react"

import Cards from "../../components/kirves/Cards.tsx"
import { handStartGame, handEndGame } from "../data/games.ts"
import { logAction } from "../logAction.ts"

const action = logAction("selectCard")
// rows after the three-player hand: Ville took tricks 1, 2 and 4, Terhi the last one
const [ville, , terhi] = handEndGame.players
const row = (player: PlayerOut, extra: Partial<React.ComponentProps<typeof Cards>> = {}) => (
  <Cards
    cards={player.playedCards}
    roundsWon={player.roundsWon}
    numOfPlayedRounds={handEndGame.numOfPlayedRounds}
    {...extra}
  />
)

export default {
  title: "Cards",
  group: "Components",
  variants: {
    myCards: () => (
      <Cards
        cards={handStartGame.myCardsInHand}
        roundsWon={[]}
        action={action}
        actionName="PLAY_CARD"
        gameId={handStartGame.id}
      />
    ),
    myCardsWithSuit: () => (
      <Cards
        cards={handStartGame.myCardsInHand}
        handTrump={handStartGame.trump}
        roundsWon={[]}
        action={action}
        actionName="PLAY_CARD"
        gameId={handStartGame.id}
      />
    ),
    otherCards: () => row(ville),
    otherCardsWithLastCard: () => row(terhi),
    otherCardsVisible: () => row(ville, { cardsVisible: true }),
  },
} satisfies PreviewPage
