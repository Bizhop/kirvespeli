// Score snapshots for the score note drawing preview (ScoreSlip), from handEndGame on. Only the
// scores change: each hand's winners get one point, a player at three points sits out, and the
// game ends when one player is left; the second loss of a match is the axe.
// test/previewScoreSteps.test.mjs checks the steps against these rules.
import { midHandGame, handEndGame } from "./games.js"

const withScores = (game, points) => ({
  ...game,
  scores: game.scores.map((s, i) => ({ ...s, score: points[i] })),
})

// The hand that ends the game: the last player under three points loses.
const finish = (game, points) => {
  const entry = withScores(game, points).scores
  const loser = entry.find(s => s.score < 3).email
  let sticks = new Set()
  for (const h of game.scoresHistory) {
    if (h.axe) sticks = new Set()
    else sticks.add(h.loser)
  }
  return {
    ...withScores(
      game,
      entry.map(() => 0),
    ),
    scoresHistory: [
      ...game.scoresHistory,
      { scores: entry.map(s => ({ ...s })), loser, axe: sticks.has(loser) },
    ],
  }
}

// points in seating order: Ville, Vesa, Terhi
const terhiStick = finish(handEndGame, [3, 3, 0])
const newGame = withScores(terhiStick, [0, 1, 1])
const terhiPoint = withScores(newGame, [0, 1, 2])
const terhiOut = withScores(terhiPoint, [0, 2, 3])
const villeAxe = finish(terhiOut, [0, 3, 3])

export const scoreSteps = [
  ["Käsi kesken", midHandGame],
  ["Ville voittaa jaon", handEndGame],
  ["Ville ja Vesa voittavat, peli päättyy: Terhille keppi", terhiStick],
  ["Uusi peli: Vesa ja Terhi voittavat jaon", newGame],
  ["Terhi voittaa jaon", terhiPoint],
  ["Vesa ja Terhi voittavat, Terhi pääsee kolmeen", terhiOut],
  ["Vesa voittaa, peli päättyy: Villelle kirves", villeAxe],
]
