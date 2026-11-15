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

// Score snapshots after handEndGame, following the scoring rules: a hand adds points, a game
// ends when one player is left under three points, and the second loss of a match is the axe.
const withScores = (game, points) => ({
  ...game,
  scores: game.scores.map((s, i) => ({ ...s, score: points[i] })),
})
const finish = (game, points) => {
  const entry = withScores(game, points).scores
  const loser = entry.find(s => s.score < 3).email
  let sticks = new Set()
  for (const h of game.scoresHistory) {
    if (h.axe) sticks = new Set()
    else sticks.add(h.loser)
  }
  return {
    ...withScores(game, [0, 0, 0]),
    scoresHistory: [
      ...game.scoresHistory,
      { scores: entry.map(s => ({ ...s })), loser, axe: sticks.has(loser) },
    ],
  }
}
// Ville, Vesa, Terhi
const terhiStick = finish(handEndGame, [3, 3, 0])
const nextHand = withScores(terhiStick, [0, 1, 1])
const threeStrokes = withScores(terhiStick, [1, 2, 3])
const villeAxe = finish(threeStrokes, [1, 3, 3])

const steps = [
  ["Käsi kesken", midHandGame],
  ["Ville saa pisteen", handEndGame],
  ["Peli päättyy: Terhille keppi", terhiStick],
  ["Uusi peli: Vesa ja Terhi saavat pisteen", nextHand],
  ["Kolme pelaajaa saa pisteitä", threeStrokes],
  ["Peli päättyy: Villelle kirves", villeAxe],
]

// Steps through confirmed snapshots. Toista shows the previous snapshot without drawing and then
// the current one again; Alusta returns to the first one without drawing.
const Steps = ({ fitViewport = false }) => {
  const [step, setStep] = useState(0)
  const [resetKey, setResetKey] = useState(0)
  const jump = (to, then) => {
    setStep(to)
    setResetKey(key => key + 1)
    if (then != null) setTimeout(() => setStep(then), 50)
  }
  return (
    <Box className="kirves-container">
      <Box sx={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: 1, mb: 1 }}>
        <Button
          variant="contained"
          disabled={step === steps.length - 1}
          onClick={() => setStep(step + 1)}
        >
          Seuraava tilanne
        </Button>
        <Button disabled={step === 0} onClick={() => jump(step - 1, step)}>
          Toista
        </Button>
        <Button onClick={() => jump(0)}>Alusta</Button>
        <span>
          {step + 1}/{steps.length}: {steps[step][0]}
        </span>
      </Box>
      <RenderGame
        game={steps[step][1]}
        motionResetKey={resetKey}
        fitViewport={fitViewport}
        {...props}
      />
    </Box>
  )
}

export default {
  title: "ScoreSlip",
  group: "Components",
  variants: {
    // Seuraava tilanne: the note opens 3 s later, after the last trick, and draws the new marks
    drawing: () => <Steps />,
    drawingFitViewport: () => <Steps fitViewport />,
    // tap the note in the bottom-right corner of the table
    note: () => <RenderGame game={midHandGame} {...props} />,
    longSheet: () => <RenderGame game={scoreSheetGame} {...props} />,
    sixPlayers: () => <RenderGame game={tableGames[6]} {...props} />,
    replay: () => <RenderGame game={scoreSheetGame} isReplay {...props} />,
  },
}
