import type { CardCode, SuitName } from "../../types/kirves.ts"

const suitCodes: Record<SuitName, string> = {
  CLUBS: "C",
  DIAMONDS: "D",
  HEARTS: "H",
  SPADES: "S",
}

// Jacks and jokers play as the trump suit; `undefined` when there is no trump yet.
export const playingSuit = (card: CardCode, trump?: SuitName | null): SuitName | undefined =>
  card[0] === "J" || card[1] === "J"
    ? (trump ?? undefined)
    : (Object.keys(suitCodes) as SuitName[]).find(suit => suitCodes[suit] === card[1])

// Keep backend indexes: hand sorting is only a display concern.
export const playableCardIndexes = (
  cards: readonly CardCode[] = [],
  firstCardSuit?: SuitName | null,
  trump?: SuitName | null,
): number[] => {
  const following = cards.flatMap((card, index) =>
    playingSuit(card, trump) === firstCardSuit ? [index] : [],
  )
  return firstCardSuit && following.length ? following : cards.map((_, index) => index)
}
