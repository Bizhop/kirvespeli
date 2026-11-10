import { games, hand } from "./games.js"

// Action log of the three-player hand in game 12 (its latest hand): DEAL, Ville's speech and
// the fifteen plays with their backend indexes.
export const logId = { gameId: games[0].id, handId: games[0].lastHandId }
export const logItems = hand.log
