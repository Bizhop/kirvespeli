import React, { useEffect, useRef, useState } from "react"
import { SvgImage } from "../shared/images"

const Card = ({
  index,
  card,
  gameId,
  action,
  actionName,
  roundsWon = [],
  cardsVisible,
  lastCard,
}) => {
  const won = roundsWon.includes(index)
  const [flipped, setFlipped] = useState(false)
  const timer = useRef()
  useEffect(() => {
    setFlipped(false)
    return () => clearTimeout(timer.current)
  }, [card, won])
  const faceUp = !won || cardsVisible || flipped
  const playable = !won && Boolean(action && actionName)
  const canFlip = won && lastCard && !cardsVisible
  const click = () => {
    if (playable) action({ gameId, action: actionName, index })
    else if (canFlip) {
      clearTimeout(timer.current)
      setFlipped(true)
      timer.current = setTimeout(() => setFlipped(false), 1500)
    }
  }
  return (
    <button
      type="button"
      className={`playing-card${playable ? " playable" : ""}`}
      disabled={!playable && !canFlip}
      onClick={click}
      aria-label={
        playable
          ? `Valitse ${card}`
          : canFlip
            ? "Näytä voittokortti"
            : faceUp
              ? card
              : "Voittokortti"
      }
    >
      <SvgImage name={faceUp ? card : "back"} className="card-image" />
      {won && cardsVisible && (
        <span className="won-card" title="Voitettu tikki">
          ✓
        </span>
      )}
    </button>
  )
}
export default Card
