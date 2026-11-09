export const games = [
  {
    id: 1,
    admin: {
      email: "ville.piispa@gmail.com",
      jwt: null,
      nickname: "Ville",
    },
    players: 1,
    lastHandId: null,
    canJoin: true,
    createdAt: "2022-08-30 06:20:29",
    updatedAt: "2022-08-30 06:20:29",
  },
]

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
  playersTotal: 1,
  firstCardSuit: "",
  scores: {
    Ville: 0,
    Vesa: 2,
    Terhi: 1,
  },
  scoresHistory: [
    {
      Ville: 3,
      Vesa: 2,
      Terhi: 3,
    },
    {
      Ville: 1,
      Vesa: 3,
      Terhi: 3,
    },
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
  scores: {
    Ville: 1,
    Vesa: 2,
    Terhi: 1,
  },
}
