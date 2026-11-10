import assert from "node:assert/strict"
import test from "node:test"
import * as fixtures from "../src/preview/data/games.js"

// Every preview game must follow the scoring rules, so score sheets look like real games.
const gamesWithScores = Object.entries(fixtures)
  .flatMap(([name, value]) =>
    value?.scores
      ? [[name, value]]
      : Object.entries(value ?? {})
          .filter(([, game]) => game?.scores)
          .map(([key, game]) => [`${name}[${key}]`, game]),
  )
  .filter(([, game]) => Array.isArray(game.scores))

test("preview fixtures with score sheets are found", () => {
  assert.ok(gamesWithScores.length > 20)
})

for (const [name, game] of gamesWithScores) {
  test(`${name}: every finished game has exactly one loser`, () => {
    const columns = game.scores.map(s => s.email)
    let sticks = new Set()
    for (const [i, entry] of (game.scoresHistory ?? []).entries()) {
      assert.deepEqual(
        entry.scores.map(s => s.email),
        columns,
        `game ${i} has the score sheet's players`,
      )
      const losers = entry.scores.filter(s => s.score < 3).map(s => s.email)
      assert.deepEqual(losers, [entry.loser], `game ${i}: only the loser is under three points`)
      // the second loss of a match is the axe and ends the match
      assert.equal(entry.axe, sticks.has(entry.loser), `game ${i}: axe follows earlier sticks`)
      if (entry.axe) sticks = new Set()
      else sticks.add(entry.loser)
    }
  })

  test(`${name}: the current game is consistent with the table`, () => {
    const players = new Map((game.players ?? []).map(p => [p.email, p]))
    const atThree = game.scores.filter(s => s.score === 3)
    for (const { email } of atThree) {
      const player = players.get(email)
      if (!player) continue
      assert.ok(
        player.inGame === false || player.departurePending,
        `${email} has three points and must be out of the hand`,
      )
    }
    assert.ok(
      game.scores.some(s => s.score < 3),
      "a game with everyone at three points would already be finished",
    )
    if ((game.myAvailableActions ?? []).includes("SET_DEALER")) {
      const last = game.scoresHistory.at(-1)
      assert.equal(last?.loser, game.players[0].email, "only the previous loser chooses the dealer")
      assert.ok(
        game.scores.every(s => s.score === 0),
        "the new game starts from zero",
      )
    }
  })
}
