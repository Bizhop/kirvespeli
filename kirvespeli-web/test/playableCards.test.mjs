import assert from "node:assert/strict"
import test from "node:test"
import { playableCardIndexes } from "../src/components/kirves/playableCards.js"

test("follow suit using original hand indexes, excluding jacks of the printed suit", () => {
  assert.deepEqual(playableCardIndexes(["JS", "AH", "AS", "BJ", "2S"], "SPADES", "HEARTS"), [2, 4])
})

test("a trump lead can be followed by any jack, joker, or regular trump", () => {
  assert.deepEqual(
    playableCardIndexes(["JS", "AH", "AS", "BJ", "RJ", "JC"], "HEARTS", "HEARTS"),
    [0, 1, 3, 4, 5],
  )
})

test("a hand with only a jack of the printed lead suit is void in that suit", () => {
  assert.deepEqual(playableCardIndexes(["JS", "AH", "BJ"], "SPADES", "HEARTS"), [0, 1, 2])
})

test("leaders and players void in the lead suit can play any card", () => {
  const hand = ["AH", "JS", "BJ"]
  for (const lead of ["", null, undefined, "DIAMONDS"])
    assert.deepEqual(playableCardIndexes(hand, lead, "HEARTS"), [0, 1, 2])
  assert.deepEqual(playableCardIndexes([], "HEARTS", "HEARTS"), [])
})
