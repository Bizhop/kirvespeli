export const hasPlayStarted = game =>
  (game.players ?? []).some(
    p => p.availableActions?.includes("PLAY_CARD") || p.playedCards?.length > 0,
  )

// FOLD is offered to waiting players too, so it does not put a player in turn.
const mustAct = player => player.availableActions?.some(a => a !== "FOLD") ?? false

// Players who are expected to act now. The backend does not always move `turn` to them
// (e.g. SPEAK_SUIT, CUT for a joining player, SET_DEALER), so actions take precedence.
export const playersInTurn = game => {
  const acting = (game.players ?? []).filter(mustAct).map(p => p.email)
  return new Set(acting.length > 0 ? acting : [game.turn].filter(Boolean))
}

// Players who reached three points and must acknowledge leaving before the next hand starts.
export const departingPlayers = game => (game.players ?? []).filter(p => p.departurePending)
