import assert from "node:assert/strict"
import test from "node:test"
import { scoreChanges, scoreSchedule, cellKey } from "../src/components/kirves/scoreChanges.ts"

const players = ["a", "b", "c"]
const snapshot = (
  scores: number[],
  history: { scores: number[]; loser: string; axe?: boolean }[] = [],
  id = 1,
) => ({
  id,
  scores: players.map((email, i) => ({ email, nickname: email, score: scores[i] })),
  scoresHistory: history.map(({ scores: s, loser, axe = false }) => ({
    scores: players.map((email, i) => ({ email, nickname: email, score: s[i] })),
    loser,
    axe,
  })),
})
const timing = { startMs: 3000, strokeMs: 100, lossMs: 200, staggerMs: 10 }

test("no previous snapshot, other game or other players: nothing is drawn", () => {
  const next = snapshot([1, 0, 0])
  assert.equal(scoreChanges(null, next), null)
  assert.equal(scoreChanges(snapshot([0, 0, 0], [], 2), next), null)
  const fewer = { ...snapshot([0, 0, 0]), scores: snapshot([0, 0, 0]).scores.slice(0, 2) }
  assert.equal(scoreChanges(fewer, next), null)
})

test("unchanged scores: nothing is drawn", () => {
  assert.equal(scoreChanges(snapshot([1, 2, 0]), snapshot([1, 2, 0])), null)
})

test("points of a hand are drawn in seat order", () => {
  const changes = scoreChanges(snapshot([1, 0, 2]), snapshot([2, 0, 3]))
  assert.deepEqual(changes, {
    cells: [
      { row: "current", email: "a", from: 1, to: 2, loss: null },
      { row: "current", email: "c", from: 2, to: 3, loss: null },
    ],
  })
  const schedule = scoreSchedule(changes!, timing)
  assert.deepEqual(schedule.cells[cellKey("current", "a")], {
    drawnStrokes: 1,
    delayMs: 3000,
    lossDelayMs: null,
  })
  assert.deepEqual(schedule.cells[cellKey("current", "c")], {
    drawnStrokes: 2,
    delayMs: 3110,
    lossDelayMs: null,
  })
  assert.equal(schedule.endMs, 3210)
})

test("a finished game draws its last points and the stick", () => {
  const prev = snapshot([3, 2, 1])
  const next = snapshot([0, 0, 0], [{ scores: [3, 3, 1], loser: "c" }])
  const changes = scoreChanges(prev, next)
  assert.deepEqual(changes, {
    cells: [
      { row: 0, email: "b", from: 2, to: 3, loss: null },
      { row: 0, email: "c", from: 1, to: 1, loss: "stick" },
    ],
  })
  const schedule = scoreSchedule(changes!, timing)
  assert.deepEqual(schedule.cells[cellKey(0, "c")], {
    drawnStrokes: 1,
    delayMs: 3000,
    lossDelayMs: 3110,
  })
  assert.equal(schedule.endMs, 3310)
})

test("the points of a finished game are drawn before the loss mark", () => {
  const prev = snapshot([2, 2, 2])
  const next = snapshot([0, 0, 0], [{ scores: [2, 3, 3], loser: "a" }])
  const schedule = scoreSchedule(scoreChanges(prev, next)!, timing)
  assert.equal(schedule.cells[cellKey(0, "b")].delayMs, 3000)
  assert.equal(schedule.cells[cellKey(0, "c")].delayMs, 3110)
  assert.equal(schedule.cells[cellKey(0, "a")].lossDelayMs, 3220)
  assert.equal(schedule.endMs, 3420)
})

test("the second loss draws the axe", () => {
  const history = [{ scores: [3, 1, 3], loser: "b" }]
  const prev = snapshot([3, 2, 2], history)
  const next = snapshot([0, 0, 0], [...history, { scores: [3, 2, 3], loser: "b", axe: true }])
  const changes = scoreChanges(prev, next)
  assert.deepEqual(changes!.cells, [
    { row: 1, email: "b", from: 2, to: 2, loss: "axe" },
    { row: 1, email: "c", from: 2, to: 3, loss: null },
  ])
})

test("missed snapshots or lower scores are shown without drawing", () => {
  const twoGames = [
    { scores: [3, 3, 1], loser: "c" },
    { scores: [3, 1, 3], loser: "b" },
  ]
  assert.equal(scoreChanges(snapshot([1, 0, 0]), snapshot([0, 0, 0], twoGames)), null)
  assert.equal(scoreChanges(snapshot([2, 0, 0]), snapshot([1, 0, 0])), null)
  assert.equal(scoreChanges(snapshot([0, 0, 0], twoGames), snapshot([0, 0, 0])), null)
})
