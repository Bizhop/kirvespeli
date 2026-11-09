import React from "react"
import { Box } from "@mui/system"

import RenderGame from "../components/kirves/RenderGame"
import { game, game2 } from "./data/games"

const action = data => alert("Action, data: " + JSON.stringify(data))

const showAllCards = () => alert("Show all cards")

export default {
  title: "Containers/RenderGame",
  component: RenderGame,
}

export const gameStart = () => (
  <Box className="kirves-container">
    <RenderGame game={game} action={action} />
  </Box>
)
export const gameEnd = () => (
  <Box className="kirves-container">
    <RenderGame game={game2} action={action} showAllCards={showAllCards} />
  </Box>
)
export const gameEndCardsVisible = () => (
  <Box className="kirves-container">
    <RenderGame game={game2} action={action} showAllCards={showAllCards} cardsVisible />
  </Box>
)
