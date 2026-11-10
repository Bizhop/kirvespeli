export const games = [
  {
    id: 1,
    admin: {
      email: "ville.piispa@gmail.com",
      jwt: null,
      nickname: "Ville",
    },
    players: 1,
    maxPlayers: 6,
    lastHandId: null,
    canJoin: true,
    createdAt: "2022-08-30 06:20:29",
    updatedAt: "2022-08-30 06:20:29",
  },
  {
    id: 3,
    admin: { email: "ville.piispa@gmail.com", nickname: "Ville" },
    players: 6,
    maxPlayers: 6,
    lastHandId: null,
    canJoin: false,
    createdAt: "2022-08-30 06:20:29",
    updatedAt: "2022-08-30 06:20:29",
  },
]

const VILLE = "ville.piispa@gmail.com"
const VESA = "vesa@example.com"
const TERHI = "terhi@example.com"
const KALLE = "kalle@example.com"

// score lists in seating order: Ville, Vesa, Terhi (and Kalle when four values are given)
const scores = (...values) =>
  [
    { email: VILLE, nickname: "Ville" },
    { email: VESA, nickname: "Vesa" },
    { email: TERHI, nickname: "Terhi" },
    { email: KALLE, nickname: "Ville" },
  ]
    .slice(0, values.length)
    .map((player, i) => ({ ...player, score: values[i] }))

// two finished matches (Terhi and Vesa got the axe) and a third in progress; the fourth player
// shares a nickname with the first
export const scoreSheetGame = {
  scores: scores(2, 0, 3, 1),
  scoresHistory: [
    { scores: scores(3, 3, 1, 3), loser: TERHI, axe: false },
    { scores: scores(0, 3, 3, 3), loser: VILLE, axe: false },
    { scores: scores(3, 3, 2, 3), loser: TERHI, axe: true },
    { scores: scores(3, 1, 3, 3), loser: VESA, axe: false },
    { scores: scores(3, 2, 3, 3), loser: VESA, axe: true },
    { scores: scores(2, 3, 3, 3), loser: VILLE, axe: false },
  ],
}

export const game = {
  players: [
    {
      email: "ville.piispa@gmail.com",
      nickname: "Ville",
      cardsInHand: 5,
      playedCards: [],
      roundsWon: [],
      availableActions: ["PLAY_CARD"],
      extraCard: null,
      declaredPlayer: true,
      folded: false,
      speak: "KEEP",
    },
  ],
  messages: ["Test message 1", "Test message 2"],
  cardsInDeck: 47,
  turn: "ville.piispa@gmail.com",
  dealer: "ville.piispa@gmail.com",
  myCardsInHand: ["QC", "QH", "4S", "4D", "QS"],
  myExtraCard: null,
  myAvailableActions: ["PLAY_CARD"],
  canJoin: false,
  canDeclineCut: false,
  trumpCard: "3S",
  trump: "SPADES",
  cutCard: "",
  secondCutCard: "",
  id: 2,
  playersTotal: 3,
  firstCardSuit: "",
  scores: scores(0, 2, 1),
  scoresHistory: [
    { scores: scores(3, 2, 3), loser: VESA, axe: false },
    { scores: scores(1, 3, 3), loser: VILLE, axe: false },
  ],
  numOfPlayedRounds: 0,
}

export const game2 = {
  ...game,
  numOfPlayedRounds: 5,
  players: [
    {
      ...game.players[0],
      cardsInHand: 0,
      playedCards: game.myCardsInHand,
      roundsWon: [0, 1, 2, 3, 4],
      availableActions: [],
    },
  ],
  myCardsInHand: [],
  scores: scores(1, 2, 1),
}
