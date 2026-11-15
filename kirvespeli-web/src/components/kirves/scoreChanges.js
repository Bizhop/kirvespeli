// Score sheet changes between two confirmed snapshots, and the order they are drawn in.
// Pure functions: ScoreSlip and ScoreCard use them, and the tests check them without React.

export const scoreOf = (scores, email) => scores.find(score => score.email === email)?.score ?? 0

// Axes per player and the open stick of the current match. An axe ends the match, so it
// clears every stick.
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

const columns = game => (game.scores ?? []).map(s => s.email)
const sameColumns = (a, b) => a.length === b.length && a.every((email, i) => email === b[i])

// New marks from prev to next, in seating order, or null when there is nothing to draw or the
// change cannot be shown as drawing: no previous snapshot, another game, other players, or
// more than one finished game at once (snapshots were missed).
//   { cells: [{ row, email, from, to, loss }], totals: { email, mark } | null }
// row is a history index, or "current" for the game in progress. from/to are stroke counts.
export const scoreChanges = (prev, next) => {
  if (!prev || !next || prev.id !== next.id) return null
  const emails = columns(next)
  if (!emails.length || !sameColumns(columns(prev), emails)) return null
  const prevHistory = prev.scoresHistory ?? []
  const nextHistory = next.scoresHistory ?? []
  const cells = []
  let totals = null
  if (nextHistory.length === prevHistory.length + 1) {
    // the game in progress finished: its row moved to the history
    const entry = nextHistory[prevHistory.length]
    for (const email of emails) {
      const from = scoreOf(prev.scores, email)
      const to = scoreOf(entry.scores, email)
      if (to < from) return null
      const loss = entry.loser === email ? (entry.axe ? "axe" : "stick") : null
      if (to > from || loss) cells.push({ row: prevHistory.length, email, from, to, loss })
    }
    for (const email of emails) {
      const to = scoreOf(next.scores, email)
      if (to > 0) cells.push({ row: "current", email, from: 0, to, loss: null })
    }
    if (entry.loser && emails.includes(entry.loser)) {
      totals = { email: entry.loser, mark: entry.axe ? "axe" : "stick" }
    }
  } else if (nextHistory.length === prevHistory.length) {
    for (const email of emails) {
      const from = scoreOf(prev.scores, email)
      const to = scoreOf(next.scores, email)
      if (to < from) return null
      if (to > from) cells.push({ row: "current", email, from, to, loss: null })
    }
  } else {
    return null
  }
  return cells.length || totals ? { cells, totals } : null
}

export const cellKey = (row, email) => `${row}:${email}`

// When each new mark is drawn, in ms from the snapshot: one pen, cell after cell, the totals
// mark last.
//   { cells: { [cellKey]: { drawnStrokes, delayMs, lossDelayMs } },
//     totals: { email, mark, delayMs } | null, startMs, endMs }
export const scoreSchedule = (changes, timing) => {
  const { startMs, strokeMs, lossMs, staggerMs } = timing
  let at = startMs
  const cells = {}
  changes.cells.forEach((cell, i) => {
    if (i > 0) at += staggerMs
    const delayMs = at
    at += (cell.to - cell.from) * strokeMs
    const lossDelayMs = cell.loss ? at : null
    if (cell.loss) at += lossMs
    cells[cellKey(cell.row, cell.email)] = { drawnStrokes: cell.from, delayMs, lossDelayMs }
  })
  let totals = null
  if (changes.totals) {
    if (changes.cells.length) at += staggerMs
    totals = { ...changes.totals, delayMs: at }
    at += lossMs
  }
  return { cells, totals, startMs, endMs: at }
}
