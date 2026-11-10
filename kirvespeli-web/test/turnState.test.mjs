import { test } from "node:test"
import assert from "node:assert/strict"
import {
  departingPlayers,
  hasPlayStarted,
  playersInTurn,
} from "../src/components/kirves/turnState.js"

const player = (email, availableActions = [], playedCards = []) => ({
  email,
  availableActions,
  playedCards,
})

test("player with a required action is in turn even when turn points elsewhere", () => {
  const game = { turn: "a", players: [player("a"), player("b", ["SPEAK_SUIT"])] }
  assert.deepEqual([...playersInTurn(game)], ["b"])
})

test("FOLD alone does not put a player in turn", () => {
  const game = { turn: "a", players: [player("a", ["PLAY_CARD"]), player("b", ["FOLD"])] }
  assert.deepEqual([...playersInTurn(game)], ["a"])
})

test("several players can be in turn at once", () => {
  const game = { turn: "a", players: [player("a", ["CUT"]), player("b", ["SET_DEALER"])] }
  assert.deepEqual([...playersInTurn(game)].sort(), ["a", "b"])
})

test("falls back to game.turn when nobody has actions", () => {
  assert.deepEqual([...playersInTurn({ turn: "b", players: [player("a"), player("b")] })], ["b"])
  assert.equal(playersInTurn({ players: [player("a")] }).size, 0)
})

test("play has started once a card is playable or played", () => {
  assert.equal(hasPlayStarted({ players: [player("a", ["SPEAK"])] }), false)
  assert.equal(hasPlayStarted({ players: [player("a", ["PLAY_CARD"])] }), true)
  assert.equal(hasPlayStarted({ players: [player("a", [], ["AS"])] }), true)
})

test("departing players are in turn until they acknowledge, regardless of game.turn", () => {
  const game = {
    turn: "c",
    players: [
      { ...player("a", [], ["AS"]), inGame: false },
      { ...player("b", ["ACKNOWLEDGE_DEPARTURE"], ["KS"]), departurePending: true },
      player("c", [], ["QS"]),
    ],
  }
  assert.deepEqual([...playersInTurn(game)], ["b"])
  assert.deepEqual(
    departingPlayers(game).map(p => p.email),
    ["b"],
  )
  assert.equal(departingPlayers({ players: [player("c")] }).length, 0)
})
