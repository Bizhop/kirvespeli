import React from "react"

import Player from "../components/kirves/Player"

const game = {
  dealer: "ville.piispa@gmail.com",
  numOfPlayedRounds: 5,
}

const player = {
  email: "ville.piispa@gmail.com",
  nickname: "Ville",
  cardsInHand: 0,
  playedCards: ["QC", "QH", "4S", "4D", "QS"],
  roundsWon: [],
  availableActions: [],
  extraCard: null,
  declaredPlayer: true,
  folded: false,
  speak: "KEEP",
}

const playerExtraCard = {
  ...player,
  speak: null,
  extraCard: "JS",
  cardsInHand: 5,
  playedCards: [],
  availableActions: ["DISCARD"],
}

const playerWithActions = {
  ...player,
  declaredPlayer: false,
  speak: null,
  cardsInHand: 5,
  playedCards: [],
  availableActions: ["PLAY_CARD", "FOLD"],
}

export default {
  title: "Containers/Player",
  component: Player,
}

export const allPlayed = () => <Player player={player} game={game} />
export const extraCard = () => <Player player={playerExtraCard} game={game} />
export const withActions = () => <Player player={playerWithActions} game={game} />
