import type { GameOut, PlayerOut } from "../../types/kirves.ts"

// Only the fields these helpers read, so live games, replays and fixtures all fit.
type TurnPlayer = Partial<
  Pick<PlayerOut, "availableActions" | "playedCards" | "departurePending">
> &
  Pick<PlayerOut, "email">
interface TurnGame {
  players?: readonly TurnPlayer[] | null
  turn?: GameOut["turn"]
}

export const hasPlayStarted = (game: TurnGame): boolean =>
  (game.players ?? []).some(
    p => p.availableActions?.includes("PLAY_CARD") || (p.playedCards?.length ?? 0) > 0,
  )

// FOLD is offered to waiting players too, and the admin may CONTINUE_GAME for departing
// players, so neither puts a player in turn.
const optionalActions = ["FOLD", "CONTINUE_GAME"]
const mustAct = (player: TurnPlayer): boolean =>
  player.availableActions?.some(a => !optionalActions.includes(a)) ?? false

// Players who are expected to act now. The backend does not always move `turn` to them
// (e.g. SPEAK_SUIT, CUT for a joining player, SET_DEALER), so actions take precedence.
export const playersInTurn = (game: TurnGame): Set<string> => {
  const acting = (game.players ?? []).filter(mustAct).map(p => p.email)
  return new Set(acting.length > 0 ? acting : [game.turn].filter((t): t is string => Boolean(t)))
}

// Players who reached three points and must acknowledge leaving before the next hand starts.
export const departingPlayers = <P extends TurnPlayer>(game: {
  players?: readonly P[] | null
}): P[] => (game.players ?? []).filter(p => p.departurePending)
