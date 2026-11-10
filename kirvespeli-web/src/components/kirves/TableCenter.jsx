import React from "react"
import { SvgImage } from "../shared/images"
import DealerSelector from "./DealerSelector"

export const Deck = ({ game }) => {
  if (!game.cardsInDeck && !game.trumpCard) return null
  const layers = Math.min(5, Math.ceil((game.cardsInDeck ?? 0) / 10))
  return (
    <div className="table-deck" aria-label={`Pakassa ${game.cardsInDeck ?? 0} korttia`}>
      {game.trumpCard && <SvgImage name={game.trumpCard} className="deck-trump" />}
      {Array.from({ length: layers }, (_, i) => (
        <SvgImage
          key={i}
          name="back"
          className="deck-card"
          style={{ transform: `translate(${i * 2}px, ${-i * 2}px)` }}
        />
      ))}
      <span className="deck-count">{game.cardsInDeck ?? 0}</span>
    </div>
  )
}
const TableCenter = ({ game, selectingDealer }) => (
  <div className="table-center">
    {selectingDealer && <DealerSelector />}
    {(game.playersTotal ?? game.players?.length ?? 0) < 2 && (
      <div className="waiting-panel">
        <p>Odotetaan pelaajia (vähintään 2)</p>
      </div>
    )}
    <div className="table-suits">
      {game.trump && (
        <div>
          Valtti
          <SvgImage name={`Suit${game.trump}`} className="suit-indicator" />
        </div>
      )}
      {game.firstCardSuit && (
        <div>
          Ajettu maa
          <SvgImage name={`Suit${game.firstCardSuit}`} className="suit-indicator" />
        </div>
      )}
    </div>
    {game.cutCard && (
      <div className="cut-cards" aria-label="Nostokortit">
        <SvgImage name={game.cutCard} className="center-card" />
        {game.secondCutCard && (
          <SvgImage name={game.secondCutCard} className="center-card opaque50" />
        )}
      </div>
    )}
  </div>
)
export default TableCenter
