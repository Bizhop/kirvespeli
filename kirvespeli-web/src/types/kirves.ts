// Domain and API contracts for the Kirves backend.
// Source: kirvespeli-backend model/kirves/out (GameOut, PlayerOut, GameBrief, ScoreOut,
// ScoreHistoryOut), model/kirves/in/GameIn, model/Card and model/User.
// These are static types only; responses are not validated at runtime.

/** Backend suit enum names, as used in `trump`, `firstCardSuit` and the `suit` request field. */
export type SuitName = "CLUBS" | "DIAMONDS" | "HEARTS" | "SPADES"
/** `Card.Suit` also contains JOKER, which is only valid on a card, not as a trump. */
export type SuitCode = "C" | "D" | "H" | "S"
export type Rank = "A" | "K" | "Q" | "J" | "T" | "9" | "8" | "7" | "6" | "5" | "4" | "3" | "2"
/** Card abbreviation (`Card.toString`): rank + suit, e.g. "AC", "TH". Jokers are "RJ" and "BJ". */
export type CardCode = `${Rank}${SuitCode}` | "RJ" | "BJ"

/** `Game.Action` */
export type ActionName =
  | "DEAL"
  | "PLAY_CARD"
  | "FOLD"
  | "CUT"
  | "ACE_OR_TWO_DECISION"
  | "SPEAK"
  | "SPEAK_SUIT"
  | "DISCARD"
  | "SET_DEALER"
  | "ACKNOWLEDGE_DEPARTURE"
  | "CONTINUE_GAME"

/** `Game.Speak` */
export type SpeakName = "CHANGE" | "KEEP" | "PASS"

/** Backend `User`. `jwt` is only present in the login response. */
export interface User {
  email: string
  nickname: string | null
  jwt?: string | null
}

/** `GameBrief`: one row of the game list. `createdAt`/`updatedAt` are "yyyy-MM-dd HH:mm:ss". */
export interface GameBrief {
  id: number
  admin: User
  players: number
  lastHandId: number | null
  canJoin: boolean
  joined: boolean
  createdAt: string
  updatedAt: string
  maxPlayers: number
}

/** `PlayerOut` */
export interface PlayerOut {
  email: string
  nickname: string | null
  cardsInHand: number
  playedCards: CardCode[]
  roundsWon: number[]
  availableActions: ActionName[]
  extraCard: CardCode | null
  declaredPlayer: boolean
  folded: boolean
  inGame: boolean
  departurePending: boolean
  speak: SpeakName | null
}

/** `ScoreOut` */
export interface ScoreOut {
  email: string
  nickname: string | null
  score: number
}

/** `ScoreHistoryOut`: a finished game. `loser` is an email, null when nobody lost. */
export interface ScoreHistoryOut {
  scores: ScoreOut[]
  loser: string | null
  axe: boolean
}

/**
 * `GameOut`: a snapshot for one viewer. `myCardsInHand`, `myExtraCard` and
 * `myAvailableActions` belong to the viewer only. A replay (`GET .../{handId}/{index}`) is
 * not viewer specific, so those fields are empty or null there. `id` is set by the
 * controller; it is not part of every producer, hence nullable.
 */
export interface GameOut {
  id: number | null
  players: PlayerOut[]
  messages: string[]
  cardsInDeck: number
  turn: string | null
  dealer: string | null
  myCardsInHand: CardCode[] | null
  myExtraCard: CardCode | null
  myAvailableActions: ActionName[] | null
  canJoin: boolean
  waitingForPlayers: boolean
  canDeclineCut: boolean
  trumpCard: CardCode | null
  trump: SuitName | null
  cutCard: CardCode | null
  secondCutCard: CardCode | null
  playersTotal: number | null
  firstCardSuit: SuitName | null
  scores: ScoreOut[] | null
  scoresHistory: ScoreHistoryOut[] | null
  departureSecondsLeft: number | null
  numOfPlayedRounds: number
}

/**
 * `GameIn`: the PUT body. The backend defaults omitted fields (`index` 0, booleans false),
 * so each action lists only the fields it uses. `index` is a backend card index into
 * `myCardsInHand`, never a position in the sorted display order.
 * Whether an action is currently allowed stays a backend decision (`availableActions`).
 */
export type GameAction =
  | { action: "DEAL" }
  | { action: "PLAY_CARD"; index: number }
  | { action: "DISCARD"; index: number }
  | { action: "FOLD" }
  | { action: "CUT"; index?: number; declineCut?: boolean }
  | { action: "ACE_OR_TWO_DECISION"; keepExtraCard: boolean }
  | { action: "SPEAK"; speak: SpeakName }
  | { action: "SPEAK_SUIT"; suit: SuitName }
  | { action: "SET_DEALER"; dealer: string }
  | { action: "ACKNOWLEDGE_DEPARTURE" }
  | { action: "CONTINUE_GAME" }

/** A `GameAction` addressed to a game, as dispatched by the `action` creator. */
export type GameActionRequest = GameAction & { gameId: number }

/** `ActionLogItem` from `GET /kirves/{id}/{handId}`; `input` is the stored `GameIn`. */
export interface ActionLogItem {
  user: User
  input: {
    action: ActionName
    index: number
    keepExtraCard: boolean
    suit: SuitName | null
    declineCut: boolean
    speak: SpeakName | null
    dealer: string | null
  }
}

/** Identifies one point of a replay: game, hand (`GameBrief.lastHandId`) and log index. */
export interface ReplayId {
  gameId: number
  handId: number
  index: number
}
