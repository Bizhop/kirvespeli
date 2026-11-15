import React, { useState } from "react"
import { Box, Button } from "@mui/material"

import RenderGame from "../../components/kirves/RenderGame"
import { midHandGame, scoreSheetGame, tableGames } from "../data/games"
import { scoreSteps as steps } from "../data/scoreSteps"
import { logAction } from "../logAction"

const props = {
  action: logAction("action"),
  join: logAction("join"),
  showAllCards: logAction("showAllCards"),
}

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
