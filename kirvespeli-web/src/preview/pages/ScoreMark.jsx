import React, { useState } from "react"
import { Box, Button } from "@mui/material"

import ScoreMark from "../../components/kirves/ScoreMark"
import { MOTION } from "../../components/kirves/motion"

const marks = [
  { score: 0 },
  { score: 1 },
  { score: 2 },
  { score: 3 },
  { score: 0, loss: "stick" },
  { score: 1, loss: "stick" },
  { score: 2, loss: "axe" },
  { score: 0, loss: "axe" },
]

const Row = ({ size, seed = "preview" }) => (
  <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2, alignItems: "center", p: 1 }}>
    {marks.map(mark => (
      <ScoreMark key={`${mark.score}${mark.loss}`} {...mark} seed={seed} size={size} />
    ))}
  </Box>
)

// Every mark drawn from empty, one after another, as new marks are drawn on the score note.
const Drawing = () => {
  const [round, setRound] = useState(0)
  let at = 300
  return (
    <>
      <Button onClick={() => setRound(round + 1)}>Toista</Button>
      <Box
        key={round}
        sx={{ display: "flex", flexWrap: "wrap", gap: 2, alignItems: "center", p: 1 }}
      >
        {marks.map(mark => {
          const delayMs = at
          const lossDelayMs = mark.loss ? at + mark.score * MOTION.scoreStrokeMs : null
          at += mark.score * MOTION.scoreStrokeMs + (mark.loss ? MOTION.scoreLossMs : 0) + 150
          return (
            <ScoreMark
              key={`${mark.score}${mark.loss}`}
              {...mark}
              seed="preview"
              size={64}
              drawnStrokes={0}
              delayMs={delayMs}
              lossDelayMs={lossDelayMs}
            />
          )
        })}
      </Box>
    </>
  )
}

export default {
  title: "ScoreMark",
  group: "Components",
  variants: {
    // 0-3 points, then loser marks: stick and axe over a faded tally
    allMarks: () => <Row />,
    drawing: () => <Drawing />,
    large: () => <Row size={64} />,
    otherSeeds: () => (
      <>
        <Row seed="a" />
        <Row seed="b" />
        <Row seed="c" />
      </>
    ),
    onFelt: () => (
      <Box sx={{ bgcolor: "#1e5631", color: "#fff", borderRadius: 1 }}>
        <Row size={40} />
      </Box>
    ),
    feltWithLossColor: () => (
      <Box
        sx={{ bgcolor: "#1e5631", color: "#fff", borderRadius: 1, "--score-loss-color": "#ffcc80" }}
      >
        <Row size={40} />
      </Box>
    ),
  },
}
