// Preview fixtures. Game snapshots are produced by playing actions with the preview engine
// (kirves.js), so they follow the backend rules: see the engine for what is derived.
import { KirvesGame, ROSTER } from "./kirves.js"

const [VILLE, VESA, TERHI] = ROSTER

// Lobby rows (GET /kirves): game 12 is the three-player game used across the previews.
export const games = [
  {
    id: 12,
    admin: { email: VILLE.email, jwt: null, nickname: VILLE.nickname },
    players: 3,
    maxPlayers: 6,
    lastHandId: 7,
    canJoin: false,
    createdAt: "2026-11-08 18:02:11",
    updatedAt: "2026-11-10 20:41:37",
  },
  {
    id: 14,
    admin: { email: TERHI.email, jwt: null, nickname: TERHI.nickname },
    players: 6,
    maxPlayers: 6,
    lastHandId: 2,
    canJoin: false,
    createdAt: "2026-11-09 19:15:03",
    updatedAt: "2026-11-10 21:02:58",
  },
  {
    id: 15,
    admin: { email: VESA.email, jwt: null, nickname: VESA.nickname },
    players: 2,
    maxPlayers: 6,
    lastHandId: null,
    canJoin: true,
    createdAt: "2026-11-10 20:55:40",
    updatedAt: "2026-11-10 20:57:12",
  },
  {
    id: 16,
    admin: { email: VILLE.email, jwt: null, nickname: VILLE.nickname },
    players: 1,
    maxPlayers: 6,
    lastHandId: null,
    canJoin: true,
    createdAt: "2026-11-10 21:04:09",
    updatedAt: "2026-11-10 21:04:09",
  },
]

// Two finished matches (Terhi and Vesa got the axe) and a third in progress, four players.
export const scoreSheetGame = new KirvesGame({
  count: 4,
  scores: [2, 0, 3, 1],
  history: [
    ["Terhi", 1],
    ["Ville", 0],
    ["Terhi", 2],
    ["Vesa", 1],
    ["Vesa", 2],
    ["Ville", 2],
  ],
}).view("Ville")

// Earlier games of the current match: Vesa and Ville have lost one each.
const matchHistory = [
  ["Vesa", 2],
  ["Ville", 1],
]

// --- One three-player hand (game 12): Terhi deals, Vesa cuts, spades are trump and Ville
// keeps. Tricks: Ville QH and QC, Vesa KD, Ville trumps TD with 4S, Terhi takes the last trick
// with JS. Ville wins the hand with three tricks.
export const threePlayerHand = (options = {}) =>
  new KirvesGame({
    count: 3,
    dealer: "Terhi",
    scores: [1, 2, 0],
    history: matchHistory,
    ...options,
  })
    .cut("8D")
    .deal(
      {
        // dealt order, not play order: the logged indexes vary like in a real game
        Ville: ["4S", "QH", "QS", "QC", "4D"],
        Vesa: ["KD", "9S", "7H", "TD", "2C"],
        Terhi: ["JS", "3D", "6H", "AH", "8C"],
      },
      "3S",
    )
    .speak("Ville", "KEEP")
export const tricks = [
  ["QH", "7H", "6H"],
  ["QC", "2C", "8C"],
  ["4D", "KD", "3D"],
  ["TD", "AH", "4S"],
  ["QS", "9S", "JS"],
].flat()
const handAfter = plays => threePlayerHand().plays(...tricks.slice(0, plays))

export const hand = handAfter(tricks.length)
export const handStartGame = handAfter(0).view("Ville")
// Ville led QH and Vesa followed; Terhi is in turn.
export const firstTrickGame = handAfter(2).view("Ville")
// Vesa led TD in the fourth trick and Terhi discarded AH; Ville has no diamonds left.
export const midHandGame = handAfter(11).view("Ville")
// Ville trumped the fourth trick and leads the last one with his last card.
export const lastCardGame = handAfter(12).view("Ville")
// After the fifth trick: Ville won the hand, Ville deals next and Terhi cuts. The played
// cards stay on the table until the cut.
export const handEndGame = hand.view("Ville")
export const game = handStartGame
export const game2 = handEndGame

// --- Table sizes: mid-hand with Ville to lead the third trick. The last seat deals and
// Ville keeps; with four or more players Vesa has folded, with five or six Terhi sits out with
// three points.
const tableHands = {
  Ville: ["AH", "AC", "QC", "QH", "4S"],
  Vesa: ["7H", "2C", "KD", "TD", "9S"],
  Terhi: ["6H", "8C", "3D", "JS", "6D"],
  Kalle: ["9H", "5C", "5D", "KS", "8D"],
  Sanna: ["2H", "9C", "7D", "2S", "QD"],
  Matti: ["4H", "TC", "9D", "6S", "AD"],
}
const tableGame = count => {
  const g = new KirvesGame({
    count,
    dealer: ROSTER[count - 1].nickname,
    scores: ROSTER.slice(0, count).map((_, i) => (count >= 5 && i === 2 ? 3 : i % 3)),
    history: matchHistory,
  })
    .cut("KH")
    .deal(tableHands, "3S")
    .speak("Ville", "KEEP")
  if (count >= 4) g.fold("Vesa")
  // everyone follows Ville's ace of hearts, then his ace of clubs
  for (let trick = 0; trick < 2; trick++) {
    g.play(trick === 0 ? "AH" : "AC")
    while (g.turn.nickname !== "Ville") g.play(g.turn.hand[0])
  }
  return g
}
const tables = Object.fromEntries([2, 3, 4, 5, 6].map(count => [count, tableGame(count)]))
export const tableGames = Object.fromEntries(
  Object.entries(tables).map(([count, g]) => [count, g.view("Ville")]),
)
export const spectatorGame = tables[6].view("Katsoja")

// --- Before and between hands
// Ville has created game 16 and waits for others; nobody can cut alone.
const waiting = new KirvesGame({ count: 1, id: 16 })
waiting.resetActions()
export const waitingGame = { ...waiting.view("Ville"), canJoin: true }

// Vesa deals; Ville sits before him and cuts.
export const cutTurnGame = new KirvesGame({ count: 3, dealer: "Vesa" }).view("Ville")
// Terhi cut the jack of clubs, so a second card was turned; she keeps the jack as an extra
// card (a forced game). Ville deals next.
export const cutGame = new KirvesGame({ count: 3, dealer: "Ville" }).cut("JC", "5H").view("Ville")

// Six players: Matti won the last hand while only he and Ville were left, so Ville lost the
// game. It was his second loss of the match, the axe. Ville deals the next game unless he
// picks another dealer; Matti, before him, cuts.
const lostGame = new KirvesGame({
  count: 6,
  dealer: "Ville",
  scores: [2, 3, 3, 3, 3, 2],
  history: matchHistory,
})
  .cut("4C")
  .deal({ Ville: ["2H", "3H", "2D", "9C", "5C"], Matti: ["AS", "KS", "AH", "KH", "AD"] }, "7S")
  .speak("Matti", "KEEP")
  .playOn()
  .acknowledge("Matti")
export const dealerGame = lostGame.view("Ville")
export const otherDealerGame = lostGame.clone().chooseDealer("Ville", "Terhi").view("Ville")

// --- Speaking and trump selection
// Ville deals; Vesa would change trump, Terhi passes and Ville speaks last.
export const speakingGame = new KirvesGame({ count: 3, dealer: "Ville", history: matchHistory })
  .cut("6C")
  .deal({ Ville: ["KD", "9H", "JS", "AD", "4C"] }, "9D")
  .speak("Vesa", "CHANGE")
  .speak("Terhi", "PASS")
  .view("Ville")
// Four players, Kalle deals: Ville and Terhi want to change hearts, so Ville (the first to
// say so) picks the new trump.
export const suitGame = new KirvesGame({ count: 4, dealer: "Kalle", scores: [1, 0, 2, 1] })
  .cut("2D")
  .deal({ Ville: ["AS", "KS", "JD", "7S", "3C"] }, "8H")
  .speak("Ville", "CHANGE")
  .speak("Vesa", "PASS")
  .speak("Terhi", "CHANGE")
  .speak("Kalle", "PASS")
  .view("Ville")

// --- Extra card: Ville deals five players and turns the ace of hearts, so three of his cards
// stay hidden while he decides whether to keep the ace.
const aceGame = () =>
  new KirvesGame({ count: 5, dealer: "Ville", scores: [0, 1, 2, 0, 1], history: matchHistory })
    .cut("9C")
    .deal({ Ville: ["QD", "7C", "KH", "4H", "JC"] }, "AH")
export const extraCardGame = aceGame().view("Ville")
// Six players, Ville turned the two of spades and kept it: he discards one card for it.
export const discardGame = new KirvesGame({
  count: 6,
  dealer: "Ville",
  scores: [0, 1, 2, 0, 1, 2],
  history: matchHistory,
})
  .cut("4C")
  .deal({ Ville: ["AH", "QC", "QH", "4S", "4D"] }, "2S")
  .aceOrTwo(true)
  .view("Ville")

// --- Hand order (Hand preview): received order differs from the display order.
const sortingHand = ["2D", "AH", "JC", "9H", "AS"]
const sortingGame = () =>
  new KirvesGame({ count: 3, dealer: "Terhi" }).cut("TC").deal({ Ville: sortingHand }, "5H")
export const handSortingGame = sortingGame().speak("Ville", "KEEP").view("Ville")
// Everyone wanted to change hearts; Ville picked spades and the hand reorders.
export const changedTrumpGame = sortingGame()
  .speak("Ville", "CHANGE")
  .speak("Vesa", "PASS")
  .speak("Terhi", "PASS")
  .speakSuit("Ville", "SPADES")
  .view("Ville")
// Ville turned the jack of hearts: he must take it and discard (a forced game).
export const handPowerGame = new KirvesGame({ count: 3, dealer: "Ville" })
  .cut("3C")
  .deal({ Ville: ["9H", "JD", "BJ", "2C", "RJ"] }, "JH")
  .view("Ville")

// --- Departures: the hand ended and players reached three points.
// Ville (the admin) won the three-player hand with two points before it.
export const departureGame = threePlayerHand({ scores: [2, 1, 1] })
  .plays(...tricks)
  .view("Ville")
// Vesa keeps with the top trumps and reaches three points; Terhi is the admin here, so Ville
// only waits. In the admin variant Ville may continue for Vesa.
const vesaWins = admin =>
  new KirvesGame({ count: 3, dealer: "Terhi", admin, scores: [1, 2, 1], history: matchHistory })
    .cut("8D")
    .deal({ Vesa: ["RJ", "BJ", "JC", "AS", "KS"] }, "3S")
    .speak("Ville", "PASS")
    .speak("Vesa", "KEEP")
    .playOn()
export const departureWaitingGame = vesaWins("Terhi").view("Ville")
export const departureAdminGame = { ...vesaWins("Ville").view("Ville"), departureSecondsLeft: 42 }
// Four players: Vesa kept but Ville took the hand, so Ville, Terhi and Kalle all score; Ville
// and Terhi reach three points.
const multiple = new KirvesGame({ count: 4, dealer: "Kalle", scores: [2, 1, 2, 1] })
  .cut("6H")
  .deal({ Ville: ["RJ", "BJ", "JC", "AS", "KS"] }, "3S")
  .speak("Ville", "PASS")
  .speak("Vesa", "KEEP")
  .playOn()
export const departureMultipleGame = multiple.view("Ville")
export const departureAcknowledgedGame = multiple.clone().acknowledge("Ville").view("Ville")
// Vesa and Terhi folded at the start, so Ville won before any card was played.
export const departureEarlyEndGame = new KirvesGame({
  count: 3,
  dealer: "Terhi",
  scores: [2, 1, 1],
  history: matchHistory,
})
  .cut("8D")
  .deal({}, "3S")
  .speak("Ville", "KEEP")
  .fold("Vesa")
  .fold("Terhi")
  .view("Ville")
// Two players left: Ville reaches three points, which will end the game with Vesa as loser.
export const departureGameEndGame = new KirvesGame({
  count: 2,
  dealer: "Vesa",
  scores: [2, 1],
  history: matchHistory,
})
  .cut("9H")
  .deal({ Ville: ["RJ", "BJ", "JC", "AS", "KS"] }, "3S")
  .speak("Ville", "KEEP")
  .playOn()
  .view("Ville")
