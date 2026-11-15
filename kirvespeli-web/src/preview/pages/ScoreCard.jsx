import React from "react"

import ScoreCard from "../../components/kirves/ScoreCard"
import { game, scoreSheetGame } from "../data/games"

// The sheet as it appears on the score note (ScoreSlip).
const paper = sheet => (
  <div className="score-slip score-slip-open score-slip-static">
    <div className="score-slip-paper">
      <ScoreCard game={sheet} />
    </div>
  </div>
)

export default {
  title: "ScoreCard",
  group: "Components",
  variants: {
    newGame: () => paper({ ...game, scoresHistory: [] }),
    twoGames: () => paper(game),
    threeMatches: () => paper(scoreSheetGame),
  },
}
