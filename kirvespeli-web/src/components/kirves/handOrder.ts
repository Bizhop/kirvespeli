import type { CardCode, SuitCode, SuitName } from "../../types/kirves.ts"

// Jacks follow the game rules: clubs > spades > hearts > diamonds.
// Use that same fixed suit order for side suits so removing cards never reshuffles them.
const SUITS: readonly SuitCode[] = ["C", "S", "H", "D"]
const RANKS = ["A", "K", "Q", "T", "9", "8", "7", "6", "5", "4", "3", "2"]
const TRUMP_SUIT: Record<SuitName, SuitCode> = {
  CLUBS: "C",
  SPADES: "S",
  HEARTS: "H",
  DIAMONDS: "D",
}

type Priority = [group: number, suit: number, rank: number]

const priority = (card: CardCode, trump: SuitCode): Priority => {
  if (card === "RJ") return [0, 0, 0]
  if (card === "BJ") return [0, 1, 0]
  const rank = card[0]
  const suit = card[1] as SuitCode
  if (rank === "J") return [1, SUITS.indexOf(suit), 0]
  if (suit === trump) return [2, 0, RANKS.indexOf(rank)]
  return [3, SUITS.indexOf(suit), RANKS.indexOf(rank)]
}

/** A card for display, with its backend index into the hand (used by PLAY_CARD and DISCARD). */
export interface IndexedCard {
  card: CardCode
  index: number
}

// Return a display list, retaining the indexes required by PLAY_CARD and DISCARD.
// The supplied hand stays untouched. Without a selected trump, preserve its order.
export const sortHandCards = (
  cards: readonly CardCode[] = [],
  trump?: SuitName | null,
): IndexedCard[] => {
  const indexed = cards.map((card, index) => ({ card, index }))
  const suit = trump ? TRUMP_SUIT[trump] : undefined
  if (!suit) return indexed
  return indexed.sort((a, b) => {
    const left = priority(a.card, suit)
    const right = priority(b.card, suit)
    return left[0] - right[0] || left[1] - right[1] || left[2] - right[2] || a.index - b.index
  })
}
