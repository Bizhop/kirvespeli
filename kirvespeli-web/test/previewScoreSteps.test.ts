import type { Snapshot } from "../src/preview/data/kirves.ts"
import assert from "node:assert/strict"
import test from "node:test"
import { scoreSteps } from "../src/preview/data/scoreSteps.ts"

// Every step of the score drawing preview is one hand under the scoring rules.
const points = (game: Snapshot) => game.scores.map(s => s.score)

for (let i = 1; i < scoreSteps.length; i++) {
  const [name, next] = scoreSteps[i]
  const [, prev] = scoreSteps[i - 1]
  test(`score step ${i + 1} (${name}) is one hand`, () => {
    const before = points(prev)
    const finished = next.scoresHistory.length === prev.scoresHistory.length + 1
    const after = finished ? next.scoresHistory.at(-1)!.scores.map(s => s.score) : points(next)
    if (!finished) assert.equal(next.scoresHistory.length, prev.scoresHistory.length)
    const inGame = before.map(p => p < 3)
    const winners = after.map((p, j) => p - before[j])
    winners.forEach((gain, j) => {
      assert.ok(gain === 0 || gain === 1, `player ${j} gains at most one point`)
      if (!inGame[j]) assert.equal(gain, 0, `player ${j} at three points sits out`)
    })
    const scored = winners.filter(Boolean).length
    assert.ok(scored >= 1, "a hand has a winner")
    assert.ok(scored < inGame.filter(Boolean).length, "a hand has a player who does not score")
    const left = after.filter(p => p < 3).length
    assert.equal(finished, left === 1, "the game ends when one player is left")
    if (finished)
      assert.ok(
        points(next).every(p => p === 0),
        "the next game starts from zero",
      )
  })
}

test("the score steps include a stick and an axe", () => {
  const last = scoreSteps.at(-1)![1].scoresHistory
  const added = last.slice(scoreSteps[0][1].scoresHistory.length)
  assert.deepEqual(
    added.map(h => h.axe),
    [false, true],
  )
})
