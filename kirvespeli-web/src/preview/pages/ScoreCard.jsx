import React from "react"

import ScoreCard from "../../components/kirves/ScoreCard"
import { game, scoreSheetGame } from "../data/games"

export default {
  title: "ScoreCard",
  group: "Components",
  variants: {
    newGame: () => <ScoreCard game={{ ...game, scoresHistory: [] }} />,
    twoGames: () => <ScoreCard game={game} />,
    threeMatches: () => <ScoreCard game={scoreSheetGame} />,
  },
}
