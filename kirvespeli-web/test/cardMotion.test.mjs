import assert from "node:assert/strict"
import test from "node:test"
import { planCardMotion, SNAP } from "../src/components/kirves/cardMotion.js"
import { sortHandCards } from "../src/components/kirves/handOrder.js"
import { KirvesGame } from "../src/preview/data/kirves.js"
import { threePlayerHand, tricks } from "../src/preview/data/games.js"

const A = "a@example.com"
const B = "b@example.com"
const C = "c@example.com"

const player = (email, playedCards = [], cardsInHand = 5) => ({
  email,
  playedCards,
  cardsInHand,
  roundsWon: [],
})
const snapshot = ({ players, hand = [], extra = null, ...rest } = {}) => ({
  id: 7,
  trump: "HEARTS",
  trumpCard: "3H",
  cutCard: "",
  secondCutCard: "",
  players: players ?? [player(A), player(B), player(C)],
  myCardsInHand: hand,
  myExtraCard: extra,
  ...rest,
})
// the same table as seen from another seat (seats rotate to start from the viewer)
const rotate = (game, steps) => ({
  ...game,
  players: [...game.players.slice(steps), ...game.players.slice(0, steps)],
})

test("identical snapshots produce no motion", () => {
  const game = snapshot({ hand: ["AH", "KS"] })
  const plan = planCardMotion(game, structuredClone(game))
  assert.deepEqual(plan, {
    snap: false,
    own: { deal: false, entering: [], leaving: [], flights: [] },
    public: { plays: [], cleared: [], entering: [], leaving: [] },
  })
})

test("first snapshot, game change and roster change snap", () => {
  const game = snapshot()
  assert.equal(planCardMotion(null, game), SNAP)
  assert.equal(planCardMotion(game, { ...game, id: 8 }), SNAP)
  assert.equal(planCardMotion(game, { ...game, players: [player(A), player(B)] }), SNAP)
  assert.equal(planCardMotion(game, rotate(game, 1)), SNAP)
})

test("own confirmed play flies from the hand to the own played row", () => {
  const prev = snapshot({ hand: ["AH", "KS", "2D"] })
  const next = snapshot({
    hand: ["AH", "2D"],
    players: [player(A, ["KS"], 2), player(B), player(C)],
  })
  const plan = planCardMotion(prev, next)
  assert.deepEqual(plan.own, {
    deal: false,
    entering: [],
    leaving: [],
    flights: [{ from: "own:KS", to: `played:${A}:KS` }],
  })
  assert.deepEqual(plan.public.plays, [])
})

test("opponent play only appears at its public destination", () => {
  const prev = snapshot({ hand: ["AH"] })
  const next = snapshot({ hand: ["AH"], players: [player(A), player(B, ["9C"], 4), player(C)] })
  const plan = planCardMotion(prev, next)
  assert.deepEqual(plan.public.plays, [`played:${B}:9C`])
  assert.deepEqual(plan.own.flights, [])
})

test("skipped plays and rewritten rows snap", () => {
  const prev = snapshot({ players: [player(A, ["AS"]), player(B), player(C)] })
  const twoAtOnce = snapshot({ players: [player(A, ["AS", "KS", "QS"]), player(B), player(C)] })
  const rewritten = snapshot({ players: [player(A, ["2S"]), player(B), player(C)] })
  assert.equal(planCardMotion(prev, twoAtOnce), SNAP)
  assert.equal(planCardMotion(prev, rewritten), SNAP)
})

test("skipped plays across different players snap for participants and spectators", () => {
  for (const viewer of ["Ville", "Terhi", "Katsoja"]) {
    for (const count of [2, 3]) {
      const game = threePlayerHand()
      const prev = game.view(viewer)
      game.plays(...tricks.slice(0, count))
      assert.equal(planCardMotion(prev, game.view(viewer)), SNAP)
    }
  }
})

test("trump change reorders kept cards without entering or leaving cards", () => {
  const prev = snapshot({ hand: ["AH", "AS", "JD"] })
  const next = snapshot({ hand: ["AH", "AS", "JD"], trump: "SPADES", trumpCard: "3S" })
  const plan = planCardMotion(prev, next)
  assert.deepEqual(plan.own, { deal: false, entering: [], leaving: [], flights: [] })
  assert.deepEqual(plan.public.entering, ["trump:3S"])
  assert.deepEqual(plan.public.leaving, ["trump:3H"])
})

test("new hand is a deal; the cleared table fades", () => {
  const prev = snapshot({
    hand: [],
    players: [player(A, ["AS", "KS"], 0), player(B, ["2C", "3C"], 0), player(C, [], 0)],
  })
  const next = snapshot({ hand: ["2H", "3D", "QS", "9C", "TD"] })
  const plan = planCardMotion(prev, next)
  assert.equal(plan.own.deal, true)
  assert.equal(plan.own.entering.length, 5)
  assert.deepEqual(plan.public.cleared, [
    `played:${A}:AS`,
    `played:${A}:KS`,
    `played:${B}:2C`,
    `played:${B}:3C`,
  ])
})

test("a retained cut extra does not prevent the five new hand cards being dealt", () => {
  const game = new KirvesGame({ count: 3, dealer: "Vesa" }).cut("JC", "9H")
  const prev = game.view("Ville")
  assert.deepEqual(prev.myCardsInHand, [])
  assert.equal(prev.myExtraCard, "JC")
  game.deal({}, "8S")
  const next = game.view("Ville")
  const plan = planCardMotion(prev, next)
  assert.equal(plan.own.deal, true)
  assert.deepEqual(
    plan.own.entering,
    next.myCardsInHand.map(card => `own:${card}`),
  )
  assert.ok(!plan.own.entering.includes("own:JC"))
  assert.deepEqual(plan.own.leaving, [])
})

test("keeping the extra card is a move, discarding a hand card is a leave", () => {
  const prev = snapshot({ hand: ["AH", "KS"], extra: "2H" })
  const kept = planCardMotion(prev, snapshot({ hand: ["AH", "KS", "2H"] }))
  assert.deepEqual(kept.own, { deal: false, entering: [], leaving: [], flights: [] })
  const discarded = planCardMotion(prev, snapshot({ hand: ["AH", "2H"] }))
  assert.deepEqual(discarded.own.leaving, ["own:KS"])
  assert.equal(discarded.own.deal, false)
})

test("identity follows the card while the action index follows the backend list", () => {
  const before = sortHandCards(["2D", "AH", "JC", "9H"], "HEARTS")
  const after = sortHandCards(["2D", "JC", "9H"], "HEARTS")
  const indexOf = (entries, card) => entries.find(entry => entry.card === card).index
  // 9H keeps its identity (card code) but now sends index 2 instead of 3
  assert.equal(indexOf(before, "9H"), 3)
  assert.equal(indexOf(after, "9H"), 2)
  assert.deepEqual(
    after.map(entry => entry.card),
    before.map(entry => entry.card).filter(card => card !== "AH"),
  )
})

test("public instructions do not depend on other players' hidden cards or order", () => {
  // B plays KS. A and C view the same public change with different private hands.
  const table = (bHand, played = []) => [
    player(A, [], 5),
    player(B, played, bHand.length),
    player(C, [], 5),
  ]
  const viewsOf = (aHand, cHand, bHandBefore, bHandAfter) => {
    const prev = snapshot({ players: table(bHandBefore) })
    const next = snapshot({ players: table(bHandAfter, ["KS"]) })
    return {
      a: planCardMotion({ ...prev, myCardsInHand: aHand }, { ...next, myCardsInHand: aHand }),
      c: planCardMotion(
        { ...rotate(prev, 2), myCardsInHand: cHand },
        { ...rotate(next, 2), myCardsInHand: cHand },
      ),
    }
  }
  const first = viewsOf(["AH", "2C"], ["3D", "4D"], ["KS", "QS", "JS"], ["QS", "JS"])
  // B's hidden hand, its order and the played card's former position all differ
  const second = viewsOf(["9C", "TC"], ["5S", "RJ"], ["2H", "3H", "KS"], ["3H", "2H"])
  for (const plan of [first.a, first.c, second.a, second.c])
    assert.deepEqual(plan.public, first.a.public)
  assert.deepEqual(first.a.public.plays, [`played:${B}:KS`])
  // nothing in any plan refers to B's hand
  for (const plan of [first.a, first.c, second.a, second.c])
    assert.ok(!JSON.stringify(plan).match(/QS|JS|2H|3H/))
})

test("spectators have no own-hand motion", () => {
  const prev = snapshot({ hand: [] })
  const next = snapshot({ hand: [], players: [player(A, ["AS"], 4), player(B), player(C)] })
  const plan = planCardMotion(prev, next)
  assert.deepEqual(plan.own, { deal: false, entering: [], leaving: [], flights: [] })
  assert.deepEqual(plan.public.plays, [`played:${A}:AS`])
})
