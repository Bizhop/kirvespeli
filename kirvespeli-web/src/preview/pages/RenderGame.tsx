import type { PreviewPage } from "../types.ts"
import type React from "react"
import { Box } from "@mui/material"

import RenderGame from "../../components/kirves/RenderGame.tsx"
import { handStartGame, firstTrickGame, midHandGame, handEndGame } from "../data/games.ts"
import { logAction } from "../logAction.ts"

const action = logAction("action")
const showAllCards = logAction("showAllCards")

// One three-player hand from Ville's seat, from the first lead to the end of the hand.
const render = (
  game: React.ComponentProps<typeof RenderGame>["game"],
  extra: Partial<React.ComponentProps<typeof RenderGame>> = {},
) => (
  <Box className="kirves-container">
    <RenderGame game={game} action={action} showAllCards={showAllCards} {...extra} />
  </Box>
)

export default {
  title: "RenderGame",
  group: "Containers",
  variants: {
    gameStart: () => render(handStartGame),
    firstTrick: () => render(firstTrickGame),
    midHand: () => render(midHandGame),
    gameEnd: () => render(handEndGame),
    gameEndCardsVisible: () => render(handEndGame, { cardsVisible: true }),
  },
} satisfies PreviewPage
