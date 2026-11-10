import React, { useEffect, useLayoutEffect, useRef, useState } from "react"
import { SvgImage } from "../shared/images"
import { prefersReducedMotion } from "./motion"

// Face changes after mount fold the previous face away and unfold the new one. Only the
// shown face is rendered, so a concealed card has no face image in the DOM.
const useFlip = face => {
  const previous = useRef(face)
  const [flip, setFlip] = useState(null)
  useLayoutEffect(() => {
    if (previous.current === face) return
    const from = previous.current
    previous.current = face
    setFlip(current => (prefersReducedMotion() ? null : { from, id: (current?.id ?? 0) + 1 }))
  }, [face])
  const done = () => setFlip(current => current && { ...current, from: null })
  return [flip, done]
}

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
  const face = faceUp ? card : "back"
  const [flip, flipDone] = useFlip(face)
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
      <SvgImage
        key={`${face}:${flip?.id ?? 0}`}
        name={face}
        alt=""
        className={`card-image${flip ? " card-flip-in" : ""}`}
      />
      {flip?.from && (
        <SvgImage
          key={`out:${flip.id}`}
          name={flip.from}
          alt=""
          className="card-image card-flip-out"
          onAnimationEnd={flipDone}
        />
      )}
      {won && cardsVisible && (
        <span className="won-card" title="Voitettu tikki">
          ✓
        </span>
      )}
    </button>
  )
}
export default Card
