import React, { useEffect, useRef } from "react"
import { Table, TableHead, TableBody, TableFooter, TableRow, TableCell, Box } from "@mui/material"

import ScoreMark from "./ScoreMark"
import { cellKey, lossTotals, scoreOf } from "./scoreChanges"

// Score sheet: one column per player in seating order, one row per finished game, the current
// game last. The loser of a game gets a stick (first loss of a match) or the axe (second loss),
// which ends the match. The footer counts axes and shows a pending stick.
// schedule (scoreChanges.js) draws the newest marks in; without it everything is shown as is.

export { lossTotals }

const cellSx = { px: 0.5, py: 0.25, textAlign: "center", "& svg": { mx: "auto" } }

const drawing = (schedule, row, email) => schedule?.cells[cellKey(row, email)] ?? {}

const HistoryRow = ({ players, entry, index, schedule }) => (
  <TableRow className={entry.axe ? "score-match-end" : undefined}>
    {players.map(player => (
      <TableCell key={player.email} sx={cellSx}>
        <ScoreMark
          score={scoreOf(entry.scores, player.email)}
          loss={entry.loser === player.email ? (entry.axe ? "axe" : "stick") : null}
          seed={`${index}:${player.email}`}
          {...drawing(schedule, index, player.email)}
        />
      </TableCell>
    ))}
  </TableRow>
)

const CurrentRow = ({ players, index, schedule }) => (
  <TableRow className="score-current" aria-label="Meneillään oleva peli">
    {players.map(player => (
      <TableCell key={player.email} sx={cellSx}>
        <ScoreMark
          score={player.score}
          seed={`${index}:${player.email}`}
          {...drawing(schedule, "current", player.email)}
        />
      </TableCell>
    ))}
  </TableRow>
)

const TotalsRow = ({ players, totals, schedule }) => (
  <TableRow>
    {players.map(player => {
      const { axes, stick } = totals[player.email]
      const drawn = schedule?.totals?.email === player.email ? schedule.totals : null
      const lossDelayMs = mark => (drawn?.mark === mark ? drawn.delayMs : null)
      return (
        <TableCell key={player.email} sx={{ ...cellSx, fontSize: "0.9rem" }}>
          <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 0.5 }}>
            {axes > 0 && (
              <ScoreMark
                loss="axe"
                seed={`total:${player.email}`}
                size={24}
                lossDelayMs={axes === 1 ? lossDelayMs("axe") : null}
              />
            )}
            {axes > 1 && (
              <span
                className={drawn?.mark === "axe" ? "score-appear" : undefined}
                style={drawn?.mark === "axe" ? { animationDelay: `${drawn.delayMs}ms` } : undefined}
              >
                ×{axes}
              </span>
            )}
            {stick && (
              <ScoreMark
                loss="stick"
                seed={`stick:${player.email}`}
                size={24}
                lossDelayMs={lossDelayMs("stick")}
              />
            )}
          </Box>
        </TableCell>
      )
    })}
  </TableRow>
)

const ScoreCard = ({ game, schedule = null }) => {
  const players = game.scores
  const history = game.scoresHistory ?? []
  const totals = lossTotals(players, history)
  const container = useRef(null)
  useEffect(() => {
    if (container.current) container.current.scrollTop = container.current.scrollHeight
  }, [history.length])
  return (
    <div className="score-card" ref={container}>
      <Table stickyHeader size="small" aria-label="Pistetaulukko">
        <TableHead>
          <TableRow>
            {players.map(player => (
              <TableCell key={player.email} sx={{ ...cellSx, px: 1 }}>
                {player.nickname}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {history.map((entry, i) => (
            <HistoryRow key={i} players={players} entry={entry} index={i} schedule={schedule} />
          ))}
          <CurrentRow players={players} index={history.length} schedule={schedule} />
        </TableBody>
        {history.length > 0 && (
          <TableFooter>
            <TotalsRow players={players} totals={totals} schedule={schedule} />
          </TableFooter>
        )}
      </Table>
    </div>
  )
}

export default ScoreCard
