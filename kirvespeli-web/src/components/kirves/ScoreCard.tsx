import type { GameOut, ScoreOut, ScoreHistoryOut } from "../../types/kirves.ts"
import type { ScoreSchedule, CellRow } from "./scoreChanges.ts"
import { useEffect, useRef } from "react"
import { Table, TableHead, TableBody, TableRow, TableCell } from "@mui/material"

import ScoreMark from "./ScoreMark.tsx"
import { cellKey, scoreOf } from "./scoreChanges.ts"

// Score sheet: one column per player in seating order, one row per finished game, the current
// game last. The loser of a game gets a stick (first loss of a match) or the axe (second loss),
// which ends the match.
// schedule (scoreChanges.ts) draws the newest marks in; without it everything is shown as is.

const cellSx = { px: 0.5, py: 0.25, textAlign: "center", "& svg": { mx: "auto" } }

const drawing = (schedule: ScoreSchedule | null | undefined, row: CellRow, email: string) =>
  schedule?.cells[cellKey(row, email)] ?? {}

const HistoryRow = ({
  players,
  entry,
  index,
  schedule,
}: {
  players: ScoreOut[]
  entry: ScoreHistoryOut
  index: number
  schedule?: ScoreSchedule | null
}) => (
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

const CurrentRow = ({
  players,
  index,
  schedule,
}: {
  players: ScoreOut[]
  index: number
  schedule?: ScoreSchedule | null
}) => (
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

const ScoreCard = ({
  game,
  schedule = null,
}: {
  game: Pick<GameOut, "scores" | "scoresHistory">
  schedule?: ScoreSchedule | null
}) => {
  const players = game.scores ?? []
  const history = game.scoresHistory ?? []
  const container = useRef<HTMLDivElement>(null)
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
      </Table>
    </div>
  )
}

export default ScoreCard
