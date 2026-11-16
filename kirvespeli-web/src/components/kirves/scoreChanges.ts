// Score sheet changes between two confirmed snapshots, and the order they are drawn in.
// Pure functions: ScoreSlip and ScoreCard use them, and the tests check them without React.
import type { GameOut, ScoreOut } from "../../types/kirves.ts"

type ScoreGame = Pick<GameOut, "id" | "scores" | "scoresHistory">

export type CellRow = number | "current"
export type Loss = "axe" | "stick"

export interface ScoreCell {
  row: CellRow
  email: string
  from: number
  to: number
  loss: Loss | null
}
export interface ScoreChanges {
  cells: ScoreCell[]
}

export interface ScoreTiming {
  startMs: number
  strokeMs: number
  lossMs: number
  staggerMs: number
}
export interface CellSchedule {
  drawnStrokes: number
  delayMs: number
  lossDelayMs: number | null
}
export interface ScoreSchedule {
  cells: Record<string, CellSchedule>
  startMs: number
  endMs: number
}

export const scoreOf = (scores: readonly ScoreOut[], email: string): number =>
  scores.find(score => score.email === email)?.score ?? 0

const columns = (game: ScoreGame): string[] => (game.scores ?? []).map(s => s.email)
const sameColumns = (a: readonly string[], b: readonly string[]): boolean =>
  a.length === b.length && a.every((email, i) => email === b[i])

// New marks from prev to next, in seating order, or null when there is nothing to draw or the
// change cannot be shown as drawing: no previous snapshot, another game, other players, or
// more than one finished game at once (snapshots were missed).
//   { cells: [{ row, email, from, to, loss }] }
// row is a history index, or "current" for the game in progress. from/to are stroke counts.
export const scoreChanges = (
  prev: ScoreGame | null | undefined,
  next: ScoreGame | null | undefined,
): ScoreChanges | null => {
  if (!prev || !next || prev.id !== next.id) return null
  const emails = columns(next)
  if (!emails.length || !sameColumns(columns(prev), emails)) return null
  const prevHistory = prev.scoresHistory ?? []
  const nextHistory = next.scoresHistory ?? []
  const cells: ScoreCell[] = []
  if (nextHistory.length === prevHistory.length + 1) {
    // the game in progress finished: its row moved to the history
    const entry = nextHistory[prevHistory.length]
    for (const email of emails) {
      const from = scoreOf(prev.scores ?? [], email)
      const to = scoreOf(entry.scores, email)
      if (to < from) return null
      const loss = entry.loser === email ? (entry.axe ? "axe" : "stick") : null
      if (to > from || loss) cells.push({ row: prevHistory.length, email, from, to, loss })
    }
    for (const email of emails) {
      const to = scoreOf(next.scores ?? [], email)
      if (to > 0) cells.push({ row: "current", email, from: 0, to, loss: null })
    }
  } else if (nextHistory.length === prevHistory.length) {
    for (const email of emails) {
      const from = scoreOf(prev.scores ?? [], email)
      const to = scoreOf(next.scores ?? [], email)
      if (to < from) return null
      if (to > from) cells.push({ row: "current", email, from, to, loss: null })
    }
  } else {
    return null
  }
  return cells.length ? { cells } : null
}

export const cellKey = (row: CellRow, email: string): string => `${row}:${email}`

// When each new mark is drawn, in ms from the snapshot: one pen, first the new points cell
// after cell in seating order, then the stick or axe.
//   { cells: { [cellKey]: { drawnStrokes, delayMs, lossDelayMs } }, startMs, endMs }
export const scoreSchedule = (changes: ScoreChanges, timing: ScoreTiming): ScoreSchedule => {
  const { startMs, strokeMs, lossMs, staggerMs } = timing
  let at = startMs
  let first = true
  const next = (durationMs: number): number => {
    if (!first) at += staggerMs
    first = false
    const delayMs = at
    at += durationMs
    return delayMs
  }
  const cells: Record<string, CellSchedule> = {}
  for (const cell of changes.cells) {
    const strokes = cell.to - cell.from
    cells[cellKey(cell.row, cell.email)] = {
      drawnStrokes: cell.from,
      delayMs: strokes > 0 ? next(strokes * strokeMs) : startMs,
      lossDelayMs: null,
    }
  }
  for (const cell of changes.cells) {
    if (cell.loss) cells[cellKey(cell.row, cell.email)].lossDelayMs = next(lossMs)
  }
  return { cells, startMs, endMs: at }
}
