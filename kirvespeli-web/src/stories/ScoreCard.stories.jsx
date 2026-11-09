import React from "react"

import ScoreCard from "../components/kirves/ScoreCard"

import { game } from "./data/games"

export default {
  title: "Components/ScoreCard",
  component: ScoreCard,
}

export const game1 = () => <ScoreCard game={game} />
export const game2 = () => <ScoreCard game={{ ...game, scoresHistory: [] }} />
