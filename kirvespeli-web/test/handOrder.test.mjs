import assert from "node:assert/strict"
import test from "node:test"
import { sortHandCards } from "../src/components/kirves/handOrder.js"

const cardsOf = entries => entries.map(entry => entry.card)
const suits = { CLUBS: "C", SPADES: "S", HEARTS: "H", DIAMONDS: "D" }
const ranks = ["A", "K", "Q", "T", "9", "8", "7", "6", "5", "4", "3", "2"]
const allCards = [
  "RJ",
  "BJ",
  ...["C", "S", "H", "D"].flatMap(suit => ["J", ...ranks].map(rank => rank + suit)),
]

for (const [trump, code] of Object.entries(suits)) {
  test(`full deck follows Kirves power order with ${trump} trump`, () => {
    const input = Object.freeze([...allCards].reverse())
    const display = sortHandCards(input, trump)
    const expected = [
      "RJ",
      "BJ",
      "JC",
      "JS",
      "JH",
      "JD",
      ...ranks.map(rank => rank + code),
      ...Object.values(suits)
        .filter(suit => suit !== code)
        .flatMap(suit => ranks.map(rank => rank + suit)),
    ]
    assert.deepEqual(cardsOf(display), expected)
    // Every visible card still points to its original action index; no input mutation.
    display.forEach(({ card, index }) => assert.equal(input[index], card))
    assert.deepEqual(input, [...allCards].reverse())
  })
}

test("before trump selection, keep the received order and indexes", () => {
  const hand = ["2H", "BJ", "AC", "RJ", "JC"]
  const expected = hand.map((card, index) => ({ card, index }))
  for (const trump of [null, undefined, ""]) assert.deepEqual(sortHandCards(hand, trump), expected)
})

test("playing and discarding use the backend index, not the displayed position", () => {
  const hand = ["2D", "AH", "BJ", "JC", "9H", "RJ"]
  assert.deepEqual(sortHandCards(hand, "HEARTS"), [
    { card: "RJ", index: 5 },
    { card: "BJ", index: 2 },
    { card: "JC", index: 3 },
    { card: "AH", index: 1 },
    { card: "9H", index: 4 },
    { card: "2D", index: 0 },
  ])
})

test("removing any card preserves the relative order of all remaining cards", () => {
  const hand = ["KD", "2C", "AS", "9D", "AC", "RJ", "JH", "AH", "2H", "KS"]
  const before = cardsOf(sortHandCards(hand, "HEARTS"))
  for (let removed = 0; removed < hand.length; removed++) {
    const remaining = hand.filter((_, index) => index !== removed)
    const display = sortHandCards(remaining, "HEARTS")
    assert.deepEqual(
      cardsOf(display),
      before.filter(card => card !== hand[removed]),
    )
    display.forEach(({ card, index }) => assert.equal(remaining[index], card))
  }
})

test("changing trump reorders only the display, keeping the same original indexes", () => {
  const hand = Object.freeze(["AH", "AS", "AC", "AD", "JD", "JC"])
  assert.deepEqual(cardsOf(sortHandCards(hand, "HEARTS")), ["JC", "JD", "AH", "AC", "AS", "AD"])
  const changed = sortHandCards(hand, "DIAMONDS")
  assert.deepEqual(cardsOf(changed), ["JC", "JD", "AD", "AC", "AS", "AH"])
  changed.forEach(({ card, index }) => assert.equal(hand[index], card))
})

test("empty and single-card hands keep valid original indexes", () => {
  assert.deepEqual(sortHandCards([], "SPADES"), [])
  assert.deepEqual(sortHandCards(undefined, "SPADES"), [])
  assert.deepEqual(sortHandCards(["2S"], "SPADES"), [{ card: "2S", index: 0 }])
})
