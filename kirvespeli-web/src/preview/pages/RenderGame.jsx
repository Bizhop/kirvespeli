import React from "react"
import { Box } from "@mui/system"

import RenderGame from "../../components/kirves/RenderGame"
import { game, game2 } from "../data/games"
import { logAction } from "../logAction"

const action = logAction("action")
const showAllCards = logAction("showAllCards")

export default {
  title: "RenderGame",
  group: "Containers",
  variants: {
    gameStart: () => (
      <Box className="kirves-container">
        <RenderGame game={game} action={action} />
      </Box>
    ),
    gameEnd: () => (
      <Box className="kirves-container">
        <RenderGame game={game2} action={action} showAllCards={showAllCards} />
      </Box>
    ),
    gameEndCardsVisible: () => (
      <Box className="kirves-container">
        <RenderGame game={game2} action={action} showAllCards={showAllCards} cardsVisible />
      </Box>
    ),
  },
}
