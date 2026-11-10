import React, { useEffect, useRef } from "react"
import {
  TableContainer,
  Paper,
  Table,
  TableHead,
  TableBody,
  TableFooter,
  TableRow,
  TableCell,
  Box,
} from "@mui/material"

import ScoreMark from "./ScoreMark"

// Score sheet: one column per player in seating order, one row per finished game, the current
// game last. The loser of a game gets a stick (first loss of a match) or the axe (second loss),
// which ends the match. The footer counts axes and shows a pending stick.

const scoreOf = (scores, email) => scores.find(score => score.email === email)?.score ?? 0

export const lossTotals = (players, history) => {
  const totals = Object.fromEntries(players.map(p => [p.email, { axes: 0, stick: false }]))
  history.forEach(entry => {
    const total = totals[entry.loser]
    if (!total) return
    if (entry.axe) {
      total.axes++
      Object.values(totals).forEach(t => (t.stick = false))
    } else {
      total.stick = true
    }
  })
  return totals
}

const cellSx = { px: 0.5, py: 0.25, textAlign: "center", "& svg": { mx: "auto" } }

const HistoryRow = ({ players, entry, index }) => (
  <TableRow
    sx={entry.axe ? { "& td": { borderBottom: 3, borderBottomColor: "text.primary" } } : {}}
  >
    {players.map(player => (
      <TableCell key={player.email} sx={cellSx}>
        <ScoreMark
          score={scoreOf(entry.scores, player.email)}
          loss={entry.loser === player.email ? (entry.axe ? "axe" : "stick") : null}
          seed={`${index}:${player.email}`}
        />
      </TableCell>
    ))}
  </TableRow>
)

const CurrentRow = ({ players, index }) => (
  <TableRow sx={{ bgcolor: "action.selected" }} aria-label="Meneillään oleva peli">
    {players.map(player => (
      <TableCell key={player.email} sx={cellSx}>
        <ScoreMark score={player.score} seed={`${index}:${player.email}`} />
      </TableCell>
    ))}
  </TableRow>
)

const TotalsRow = ({ players, totals }) => (
  <TableRow>
    {players.map(player => {
      const { axes, stick } = totals[player.email]
      return (
        <TableCell key={player.email} sx={{ ...cellSx, color: "text.primary", fontSize: "0.9rem" }}>
          <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 0.5 }}>
            {axes > 0 && <ScoreMark loss="axe" seed={`total:${player.email}`} size={24} />}
            {axes > 1 && <span>×{axes}</span>}
            {stick && <ScoreMark loss="stick" seed={`stick:${player.email}`} size={24} />}
          </Box>
        </TableCell>
      )
    })}
  </TableRow>
)

const ScoreCard = ({ game }) => {
  const players = game.scores
  const history = game.scoresHistory
  const totals = lossTotals(players, history)
  const container = useRef(null)
  useEffect(() => {
    if (container.current) container.current.scrollTop = container.current.scrollHeight
  }, [history.length])
  return (
    <TableContainer component={Paper} ref={container} sx={{ maxHeight: 420, width: "auto" }}>
      <Table stickyHeader size="small" aria-label="Pistetaulukko">
        <TableHead>
          <TableRow>
            {players.map(player => (
              <TableCell key={player.email} sx={{ ...cellSx, fontWeight: "bold", px: 1 }}>
                {player.nickname}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {history.map((entry, i) => (
            <HistoryRow key={i} players={players} entry={entry} index={i} />
          ))}
          <CurrentRow players={players} index={history.length} />
        </TableBody>
        {history.length > 0 && (
          <TableFooter>
            <TotalsRow players={players} totals={totals} />
          </TableFooter>
        )}
      </Table>
    </TableContainer>
  )
}

export default ScoreCard
