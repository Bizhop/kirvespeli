const suitCodes = { CLUBS: "C", DIAMONDS: "D", HEARTS: "H", SPADES: "S" }

export const playingSuit = (card, trump) =>
  card[0] === "J" || card[1] === "J"
    ? trump
    : Object.keys(suitCodes).find(suit => suitCodes[suit] === card[1])

// Keep backend indexes: hand sorting is only a display concern.
export const playableCardIndexes = (cards = [], firstCardSuit, trump) => {
  const following = cards.flatMap((card, index) =>
    playingSuit(card, trump) === firstCardSuit ? [index] : [],
  )
  return firstCardSuit && following.length ? following : cards.map((_, index) => index)
}
