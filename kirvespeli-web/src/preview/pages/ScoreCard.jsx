import React from "react"

import ScoreCard from "../../components/kirves/ScoreCard"
import { game } from "../data/games"

export default {
  title: "ScoreCard",
  group: "Components",
  variants: {
    game1: () => <ScoreCard game={game} />,
    game2: () => <ScoreCard game={{ ...game, scoresHistory: [] }} />,
  },
}
