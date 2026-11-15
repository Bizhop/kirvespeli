import React, { useState } from "react"
import { Box, Button } from "@mui/material"

import RenderGame from "../../components/kirves/RenderGame"
import { midHandGame, handEndGame, scoreSheetGame, tableGames } from "../data/games"
import { logAction } from "../logAction"

const props = {
  action: logAction("action"),
  join: logAction("join"),
  showAllCards: logAction("showAllCards"),
}

// The hand ends: Ville's second point (handEndGame), then the game ends with Ville and Vesa at
// three points and Terhi's stick.
const gameEnd = {
  ...handEndGame,
  scores: handEndGame.scores.map(s => ({ ...s, score: 0 })),
  scoresHistory: [
    ...handEndGame.scoresHistory,
    {
      scores: handEndGame.scores.map(s => ({
        ...s,
        score: s.email === "terhi@example.com" ? 0 : 3,
      })),
      loser: "terhi@example.com",
      axe: false,
    },
  ],
}

// Steps through confirmed snapshots; the reset shows the first one again without drawing.
const Steps = ({ steps }) => {
  const [step, setStep] = useState(0)
  const [resetKey, setResetKey] = useState(0)
  const last = step === steps.length - 1
  return (
    <Box className="kirves-container">
      <Box sx={{ display: "flex", gap: 1, mb: 1 }}>
        <Button variant="contained" disabled={last} onClick={() => setStep(step + 1)}>
          Seuraava tilanne
        </Button>
        <Button
          onClick={() => {
            setStep(0)
            setResetKey(key => key + 1)
          }}
        >
          Alusta
        </Button>
      </Box>
      <RenderGame game={steps[step]} motionResetKey={resetKey} {...props} />
    </Box>
  )
}

export default {
  title: "ScoreSlip",
  group: "Components",
  variants: {
    // tap the note in the bottom-right corner of the table
    note: () => <RenderGame game={midHandGame} {...props} />,
    longSheet: () => <RenderGame game={scoreSheetGame} {...props} />,
    sixPlayers: () => <RenderGame game={tableGames[6]} {...props} />,
    // the note opens after the last trick has been shown and the new marks are drawn
    drawHand: () => <Steps steps={[midHandGame, handEndGame, gameEnd]} />,
    replay: () => <RenderGame game={scoreSheetGame} isReplay {...props} />,
  },
}
