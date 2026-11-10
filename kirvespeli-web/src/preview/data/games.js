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

// Complete seat fixtures; preserve the older fixtures used by individual previews.
const tablePlayers = ["Ville", "Vesa", "Terhi", "Kalle", "Sanna", "Matti"].map((nickname, i) => ({
  email: i === 0 ? VILLE : `${nickname.toLowerCase()}@example.com`,
  nickname,
  cardsInHand: 3,
  playedCards: ["7H", "QC"],
  roundsWon: i === 0 ? [0, 1] : [],
  availableActions: i === 0 ? ["PLAY_CARD"] : [],
  extraCard: null,
  declaredPlayer: i === 0,
  folded: false,
  inGame: true,
  speak: "KEEP",
}))
export const tableGames = Object.fromEntries(
  [2, 3, 4, 5, 6].map(count => [
    count,
    {
      ...game,
      players: tablePlayers.slice(0, count).map((p, i) => ({
        ...p,
        folded: count >= 4 && i === 1,
        inGame: !(count >= 4 && i === 1) && !(count >= 5 && i === 2),
        cardsInHand: (count >= 4 && i === 1) || (count >= 5 && i === 2) ? 0 : 3,
        playedCards: (count >= 4 && i === 1) || (count >= 5 && i === 2) ? [] : p.playedCards,
      })),
      playersTotal: count,
      myCardsInHand: ["QC", "QH", "4S"],
      cardsInDeck: 52 - count * 5,
      numOfPlayedRounds: 2,
      firstCardSuit: "HEARTS",
      dealer: tablePlayers[count - 1].email,
      scores: tablePlayers.slice(0, count).map((p, i) => ({
        email: p.email,
        nickname: p.nickname,
        score: count >= 5 && i === 2 ? 3 : i % 3,
      })),
      scoresHistory: [],
    },
  ]),
)
const phase = (count, actions, extra = {}) => ({
  ...tableGames[count],
  myAvailableActions: actions,
  firstCardSuit: "",
  numOfPlayedRounds: 0,
  players: tableGames[count].players.map((p, i) => ({
    ...p,
    playedCards: [],
    roundsWon: [],
    folded: false,
    inGame: true,
    cardsInHand: 5,
    speak: i === 1 ? "CHANGE" : null,
    availableActions: i === 0 ? actions : [],
  })),
  ...extra,
})
export const waitingGame = phase(2, [], {
  players: [
    { ...tablePlayers[0], availableActions: [], playedCards: [], roundsWon: [], cardsInHand: 0 },
  ],
  playersTotal: 1,
  canJoin: true,
  myCardsInHand: [],
  trump: "",
  trumpCard: "",
  cardsInDeck: 52,
  scores: [{ email: VILLE, nickname: "Ville", score: 0 }],
})
export const dealerGame = phase(6, ["SET_DEALER", "CUT"], {
  myCardsInHand: [],
  trumpCard: "",
  trump: "",
})
export const cutGame = phase(2, ["DEAL"], {
  myCardsInHand: [],
  cutCard: "AS",
  secondCutCard: "2S",
  trumpCard: "",
  trump: "",
})
export const speakingGame = phase(3, ["SPEAK"])
export const suitGame = phase(4, ["SPEAK_SUIT"])
export const extraCardGame = phase(5, ["ACE_OR_TWO_DECISION"], {
  myExtraCard: "AH",
  myCardsInHand: ["QC", "QH", "4S", "4D", "QS"],
})
export const discardGame = phase(6, ["DISCARD"], {
  myCardsInHand: ["AH", "QC", "QH", "4S", "4D", "QS"],
})
export const spectatorGame = {
  ...tableGames[6],
  myCardsInHand: [],
  myAvailableActions: [],
  turn: tablePlayers[3].email,
}

// Unsorted server hands for checking display order and original action indexes.
export const handSortingGame = {
  ...game,
  trump: "HEARTS",
  myCardsInHand: ["2D", "AH", "JC", "9H", "AS"],
}
export const handPowerGame = {
  ...discardGame,
  trump: "HEARTS",
  myCardsInHand: ["9H", "JD", "BJ", "2C", "RJ", "JC"],
}
