import React from "react"
import { Box } from "@mui/system"

import RenderGame from "../../components/kirves/RenderGame"
import { handStartGame, firstTrickGame, midHandGame, handEndGame } from "../data/games"
import { logAction } from "../logAction"

const action = logAction("action")
const showAllCards = logAction("showAllCards")

// One three-player hand from Ville's seat, from the first lead to the end of the hand.
const render = (game, extra = {}) => (
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
}
