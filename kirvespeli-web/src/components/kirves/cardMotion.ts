// Compare two confirmed game snapshots and describe the card motion they allow.
//
// Privacy boundary: `own` is derived only from the viewer's private fields (myCardsInHand,
// myExtraCard) and is shown only to that viewer. `public` is derived only from public fields
// (played cards, cut and trump cards), so every viewer gets the same public instructions for
// the same public change. Other players' hands are never part of the plan.

import type { CardCode, GameOut, PlayerOut } from "../../types/kirves.ts"

export const ownKey = (card: CardCode): string => `own:${card}`
export const playedKey = (email: string, card: CardCode): string => `played:${email}:${card}`
export const cutKey = (card: CardCode): string => `cut:${card}`
export const trumpKey = (card: CardCode): string => `trump:${card}`
export const DECK_KEY = "deck"

// Only the fields the plan reads; live games, replays and fixtures all fit.
type MotionPlayer = Partial<Pick<PlayerOut, "playedCards">> & Pick<PlayerOut, "email">
export type MotionGame = Partial<
  Pick<GameOut, "id" | "myCardsInHand" | "myExtraCard" | "cutCard" | "secondCutCard" | "trumpCard">
> & { players?: readonly MotionPlayer[] | null }

export interface Snap {
  readonly snap: true
}
export interface Flight {
  from: string
  to: string
}
export interface MotionPlan {
  snap: false
  own: { deal: boolean; entering: string[]; leaving: string[]; flights: Flight[] }
  public: { plays: string[]; cleared: string[]; entering: string[]; leaving: string[] }
}

export const SNAP: Snap = Object.freeze({ snap: true })

const ownCards = (game: MotionGame): CardCode[] => [
  ...(game.myCardsInHand ?? []),
  ...(game.myExtraCard ? [game.myExtraCard] : []),
]

const tableKeys = (game: MotionGame): string[] =>
  [
    ...[game.cutCard, game.secondCutCard].filter((c): c is CardCode => Boolean(c)).map(cutKey),
    ...(game.trumpCard ? [trumpKey(game.trumpCard)] : []),
  ].sort()

const isPrefix = <T>(prefix: readonly T[], list: readonly T[]): boolean =>
  prefix.every((card, i) => list[i] === card)

const difference = <T>(list: readonly T[], other: readonly T[]): T[] =>
  list.filter(item => !other.includes(item))

export const planCardMotion = (
  prev: MotionGame | null | undefined,
  next: MotionGame | null | undefined,
): MotionPlan | Snap => {
  if (!prev || !next || prev.id == null || prev.id !== next.id) return SNAP
  const emails = (game: MotionGame): string[] => (game.players ?? []).map(p => p.email)
  const prevEmails = emails(prev)
  const nextEmails = emails(next)
  if (prevEmails.length !== nextEmails.length || prevEmails.some((e, i) => e !== nextEmails[i]))
    return SNAP

  // Public played cards: at most one appended card across the whole table, or cleared rows.
  const plays: { email: string; card: CardCode }[] = []
  const cleared: string[] = []
  for (const [i, player] of (next.players ?? []).entries()) {
    const before = prev.players?.[i]?.playedCards ?? []
    const after = player.playedCards ?? []
    if (isPrefix(before, after)) {
      const appended = after.slice(before.length)
      // several plays by one player means skipped snapshots
      if (appended.length > 1) return SNAP
      appended.forEach(card => plays.push({ email: player.email, card }))
    } else if (after.length === 0) {
      before.forEach(card => cleared.push(playedKey(player.email, card)))
    } else return SNAP
  }
  if (plays.length > 1) return SNAP

  // The viewer's own cards; the extra card shares the hand identity so keeping it moves it in.
  const before = ownCards(prev)
  const after = ownCards(next)
  const keptHandCard = (next.myCardsInHand ?? []).some(card =>
    (prev.myCardsInHand ?? []).includes(card),
  )
  const entering = difference(after, before)
  const leaving = difference(before, after)
  const ownPlays = plays.filter(play => leaving.includes(play.card))
  if (ownPlays.length > 1) return SNAP
  const flights = ownPlays.map(({ email, card }) => ({
    from: ownKey(card),
    to: playedKey(email, card),
  }))
  const flown = new Set(ownPlays.map(play => play.card))

  const prevTable = tableKeys(prev)
  const nextTable = tableKeys(next)
  return {
    snap: false,
    own: {
      // A cut extra can survive a deal; only retained hand cards rule out a fresh hand.
      deal: entering.length > 0 && !keptHandCard,
      entering: entering.map(ownKey),
      leaving: leaving.filter(card => !flown.has(card)).map(ownKey),
      flights,
    },
    public: {
      plays: plays
        .filter(play => !flown.has(play.card))
        .map(play => playedKey(play.email, play.card))
        .sort(),
      cleared: cleared.sort(),
      entering: difference(nextTable, prevTable),
      leaving: difference(prevTable, nextTable),
    },
  }
}
