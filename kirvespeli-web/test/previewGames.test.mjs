import assert from "node:assert/strict"
import test from "node:test"
import { KirvesGame, winningIndex, ALL_CARDS } from "../src/preview/data/kirves.js"
import * as fixtures from "../src/preview/data/games.js"

test("trick winner follows the backend card order", () => {
  // trump beats the led suit; jacks and jokers are trump
  assert.equal(winningIndex(["AH", "2S"], "SPADES"), 1)
  assert.equal(winningIndex(["AH", "JD"], "SPADES"), 1)
  // jacks: clubs > spades > hearts > diamonds, below the jokers
  assert.equal(winningIndex(["JD", "JC", "JS"], "HEARTS"), 1)
  assert.equal(winningIndex(["JC", "BJ", "RJ"], "HEARTS"), 2)
  // an off-suit card never wins, whatever its rank
  assert.equal(winningIndex(["2H", "AC"], "SPADES"), 0)
  assert.equal(winningIndex(["9H", "KH", "QH"], "SPADES"), 1)
})

test("the deck has 54 distinct cards", () => {
  assert.equal(new Set(ALL_CARDS).size, 54)
})

test("deck size follows the cut and the deal", () => {
  const game = new KirvesGame({ count: 4, dealer: "Kalle" }).cut("JC", "5H")
  assert.equal(game.view("Ville").cardsInDeck, 52)
  game.deal({}, "8S")
  // 54 - two cut cards - 4 x 5 - the trump card
  assert.equal(game.view("Ville").cardsInDeck, 31)
})

test("an ace as trump card hides three of the dealer's cards until the decision", () => {
  const game = new KirvesGame({ count: 3, dealer: "Ville" }).cut("9C").deal({}, "AS")
  const view = game.view("Ville")
  assert.equal(view.myCardsInHand.length, 2)
  assert.equal(view.myExtraCard, "AS")
  assert.equal(view.trumpCard, "")
  assert.deepEqual(view.myAvailableActions, ["ACE_OR_TWO_DECISION"])
  game.aceOrTwo(true)
  assert.equal(game.view("Ville").myCardsInHand.length, 5)
  assert.deepEqual(game.view("Ville").myAvailableActions, ["DISCARD"])
})

test("illegal actions are rejected", () => {
  const game = new KirvesGame({ count: 3, dealer: "Terhi" }).cut("8D").deal({}, "3S")
  assert.throws(() => game.play(game.turn.hand[0]), /cannot play/)
  assert.throws(() => game.speak("Vesa", "KEEP"), /cannot SPEAK/)
})

const views = Object.entries(fixtures)
  .flatMap(([name, value]) =>
    value?.players
      ? [[name, value]]
      : Object.entries(value ?? {})
          .filter(([, game]) => game?.players)
          .map(([key, game]) => [`${name}[${key}]`, game]),
  )
  .filter(([, game]) => Array.isArray(game.players) && game.myAvailableActions)

for (const [name, game] of views) {
  test(`${name}: every card is in one place only`, () => {
    const cards = [
      ...game.myCardsInHand,
      ...game.players.flatMap(p => p.playedCards),
      // a cut jack or joker stays on the table while the cutter holds it as an extra card
      ...game.players.map(p => p.extraCard).filter(card => card && card !== game.cutCard),
      game.trumpCard,
      game.cutCard,
      game.secondCutCard,
    ].filter(Boolean)
    assert.equal(new Set(cards).size, cards.length, cards.join(" "))
  })

  test(`${name}: counts and won tricks match the cards`, () => {
    const total = game.players.reduce((sum, p) => sum + p.roundsWon.length, 0)
    assert.equal(game.numOfPlayedRounds, total)
    for (const p of game.players) {
      for (const round of p.roundsWon) assert.ok(round < p.playedCards.length)
      if (p.folded) assert.equal(p.cardsInHand + p.playedCards.length, 0)
    }
    // a seated viewer is the first seat; a spectator has no cards or actions of their own
    const seatedViewer = game.players[0]
    if (game.myCardsInHand.length) {
      assert.equal(seatedViewer.cardsInHand, game.myCardsInHand.length)
      assert.deepEqual(seatedViewer.availableActions, game.myAvailableActions)
    }
  })
}
