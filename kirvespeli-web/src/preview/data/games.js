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

// Finished games as [loser email, loser's points]; everyone else reached three points, so a
// finished game has exactly one loser. A player's second loss within a match is the axe,
// which ends the match.
const completedGames = (players, losses) => {
  let sticks = new Set()
  return losses.map(([loser, points]) => {
    const axe = sticks.has(loser)
    if (axe) sticks = new Set()
    else sticks.add(loser)
    return {
      scores: players.map(({ email, nickname }) => ({
        email,
        nickname,
        score: email === loser ? points : 3,
      })),
      loser,
      axe,
    }
  })
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
      // Terhi sits out with three points in five- and six-player games
      scores: tablePlayers.slice(0, count).map((p, i) => ({
        email: p.email,
        nickname: p.nickname,
        score: count >= 5 && i === 2 ? 3 : i % 3,
      })),
      // Vesa and Ville have lost one game each in this match
      scoresHistory: completedGames(tablePlayers.slice(0, count), [
        [VESA, 2],
        [VILLE, 1],
      ]),
    },
  ]),
)
// Before play everyone is in the hand, so nobody has three points yet.
const phase = (count, actions, extra = {}) => ({
  ...tableGames[count],
  scores: tableGames[count].scores.map(s => ({ ...s, score: Math.min(s.score, 2) })),
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
  scoresHistory: [],
})
// Ville just lost the game (his second loss, the axe) and chooses the next dealer; the new
// game starts from zero.
export const dealerGame = phase(6, ["SET_DEALER", "CUT"], {
  myCardsInHand: [],
  trumpCard: "",
  trump: "",
  scores: tableGames[6].scores.map(s => ({ ...s, score: 0 })),
  scoresHistory: completedGames(tablePlayers, [
    [VESA, 2],
    [VILLE, 1],
    [VILLE, 2],
  ]),
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

// A hand has ended and the players at `pending` seat indexes reached three points. `acknowledged`
// seats have already left; their cards stay on the table until everyone has acknowledged.
const handCards = [
  ["7H", "QC", "KD", "9S", "TH"],
  ["8H", "AC", "3D", "4S", "JH"],
  ["5H", "6C", "QD", "KS", "2H"],
  ["9H", "TC", "AD", "6S", "3H"],
]
const departure = (count, pending, { acknowledged = [], tricks = 5, folded = [] } = {}) => {
  const players = tableGames[count].players.map((p, i) => {
    const out = folded.includes(i)
    return {
      ...p,
      folded: out,
      inGame: !out && !pending.includes(i) && !acknowledged.includes(i),
      departurePending: pending.includes(i),
      availableActions: pending.includes(i) ? ["ACKNOWLEDGE_DEPARTURE"] : [],
      cardsInHand: out ? 0 : 5 - tricks,
      playedCards: out ? [] : handCards[i].slice(0, tricks),
      roundsWon: [],
      speak: null,
      declaredPlayer: i === 1,
    }
  })
  // the first departing seat took the tricks
  const winner = players[pending[0] ?? acknowledged[0]]
  winner.roundsWon = Array.from({ length: Math.min(tricks, 3) }, (_, i) => i)
  return {
    ...tableGames[count],
    players,
    turn: tablePlayers[1].email,
    myAvailableActions: players[0].availableActions,
    myCardsInHand: players[0].cardsInHand ? handCards[0].slice(tricks) : [],
    numOfPlayedRounds: winner.roundsWon.length,
    departureSecondsLeft: 60,
    firstCardSuit: "",
    scores: players.map((p, i) => ({
      email: p.email,
      nickname: p.nickname,
      score: pending.includes(i) || acknowledged.includes(i) ? 3 : 1,
    })),
  }
}
export const departureGame = departure(3, [0])
export const departureWaitingGame = departure(3, [1])
const adminContinue = departure(3, [1])
export const departureAdminGame = {
  ...adminContinue,
  myAvailableActions: ["CONTINUE_GAME"],
  players: adminContinue.players.map((p, i) =>
    i === 0 ? { ...p, availableActions: ["CONTINUE_GAME"] } : p,
  ),
  departureSecondsLeft: 42,
}
export const departureMultipleGame = departure(4, [0, 2])
export const departureAcknowledgedGame = departure(4, [2], { acknowledged: [0] })
export const departureEarlyEndGame = departure(3, [0], { tricks: 2, folded: [1, 2] })
export const departureGameEndGame = departure(2, [0])

// One realistic three-player hand (Ville, Vesa, Terhi; Terhi deals, spades are trump, Ville
// declared). Tricks: Ville QH, QC; Vesa KD; Ville trumps TD with 4S; Terhi takes the last
// trick with JS. Seat order is the viewer's (Ville first); score lists are in seating order.
const handPlayers = [
  { email: VILLE, nickname: "Ville", speak: "KEEP", declaredPlayer: true },
  { email: VESA, nickname: "Vesa", speak: "PASS", declaredPlayer: false },
  { email: TERHI, nickname: "Terhi", speak: "PASS", declaredPlayer: false },
]
const handPlays = {
  [VILLE]: ["QH", "QC", "4D", "4S", "QS"],
  [VESA]: ["7H", "2C", "KD", "TD", "9S"],
  [TERHI]: ["6H", "8C", "3D", "AH", "JS"],
}
const trickWinners = [VILLE, VILLE, VESA, VILLE, TERHI]
// played[email] = number of cards that player has played so far
const handState = ({ played, turn, actions = {}, ...rest }) => {
  const rounds = Math.min(...Object.values(played))
  const players = handPlayers.map(p => ({
    ...p,
    cardsInHand: 5 - played[p.email],
    playedCards: handPlays[p.email].slice(0, played[p.email]),
    roundsWon: trickWinners.slice(0, rounds).flatMap((w, i) => (w === p.email ? [i] : [])),
    availableActions: actions[p.email] ?? [],
    extraCard: null,
    folded: false,
    inGame: true,
  }))
  return {
    id: 12,
    players,
    messages: [],
    cardsInDeck: 38,
    turn,
    dealer: TERHI,
    myCardsInHand: handPlays[VILLE].slice(played[VILLE]),
    myExtraCard: null,
    myAvailableActions: actions[VILLE] ?? [],
    canJoin: false,
    canDeclineCut: false,
    trumpCard: "3S",
    trump: "SPADES",
    cutCard: "",
    secondCutCard: "",
    playersTotal: 3,
    firstCardSuit: "",
    scores: scores(1, 2, 0),
    scoresHistory: game.scoresHistory,
    numOfPlayedRounds: rounds,
    ...rest,
  }
}
export const handStartGame = handState({
  played: { [VILLE]: 0, [VESA]: 0, [TERHI]: 0 },
  turn: VILLE,
  actions: { [VILLE]: ["PLAY_CARD"] },
})
export const firstTrickGame = handState({
  played: { [VILLE]: 1, [VESA]: 1, [TERHI]: 0 },
  turn: TERHI,
  actions: { [TERHI]: ["PLAY_CARD"] },
  firstCardSuit: "HEARTS",
})
// Vesa led TD in the fourth trick and Terhi discarded AH; Ville has no diamonds left.
export const midHandGame = handState({
  played: { [VILLE]: 3, [VESA]: 4, [TERHI]: 4 },
  turn: VILLE,
  actions: { [VILLE]: ["PLAY_CARD"] },
  firstCardSuit: "DIAMONDS",
})
// After the fifth trick: Ville won the hand, Ville deals next and Terhi cuts. The played
// cards stay on the table until the cut.
export const handEndGame = handState({
  played: { [VILLE]: 5, [VESA]: 5, [TERHI]: 5 },
  turn: TERHI,
  actions: { [TERHI]: ["CUT"] },
  dealer: VILLE,
  trumpCard: "",
  trump: "",
  messages: ["Voittaja: Ville"],
  scores: scores(2, 2, 0),
})
