import React from "react"
import { values, keys } from "ramda"
import {
  TableContainer,
  Paper,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Box,
} from "@mui/material"

const ScoreRow = ({ scores, unique, borders }) => (
  <TableRow sx={borders ? { border: 2, borderColor: "lightblue" } : {}}>
    {values(scores).map((score, i) => (
      <TableCell key={`score-${unique}${i}`}>{score}</TableCell>
    ))}
  </TableRow>
)

const ScoreCard = ({ game }) => (
  <Box>
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            {keys(game.scores).map((player, i) => (
              <TableCell key={`player-${i}`}>{player}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {game.scoresHistory.map((history, i) => (
            <ScoreRow scores={values(history)} unique={i} key={`history-${i}`} />
          ))}
          <ScoreRow scores={game.scores} unique={0} borders />
        </TableBody>
      </Table>
    </TableContainer>
  </Box>
)

export default ScoreCard
