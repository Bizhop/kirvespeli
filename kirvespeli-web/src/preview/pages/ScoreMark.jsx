import React from "react"
import { Box } from "@mui/material"

import ScoreMark from "../../components/kirves/ScoreMark"

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
  <Box sx={{ display: "flex", gap: 2, alignItems: "center", p: 1 }}>
    {marks.map(mark => (
      <ScoreMark key={`${mark.score}${mark.loss}`} {...mark} seed={seed} size={size} />
    ))}
  </Box>
)

export default {
  title: "ScoreMark",
  group: "Components",
  variants: {
    // 0-3 points, then loser marks: stick and axe over a faded tally
    allMarks: () => <Row />,
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
