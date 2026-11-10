import React, { useEffect, useRef, useState } from "react"
import { SvgImage } from "../shared/images"
import { prefersReducedMotion } from "./motion"
import { useCardMotionEnabled } from "./CardMotion"

// Face changes after mount fold the previous face away and unfold the new one. Only the
// shown face is rendered, so a concealed card has no face image in the DOM.
const useFlip = (face, enabled) => {
  const [state, setState] = useState({ face, flip: null })
  let flip = state.flip
  if (state.face !== face || (!enabled && flip)) {
    // adjust state during render so the new face never mounts without its animation
    flip = !enabled || prefersReducedMotion() ? null : { from: state.face, id: (flip?.id ?? 0) + 1 }
    setState({ face, flip })
  }
  const done = () =>
    setState(current => ({ ...current, flip: current.flip && { ...current.flip, from: null } }))
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
  legal = true,
}) => {
  const won = roundsWon.includes(index)
  const motionEnabled = useCardMotionEnabled()
  // Also reveal a winning card that arrives already won in the last player's snapshot.
  const [win, setWin] = useState({ card, won, revealing: won && lastCard && motionEnabled })
  const winChanged = win.card !== card || win.won !== won
  if (winChanged) {
    setWin({ card, won, revealing: won && motionEnabled })
  }
  const revealing = (winChanged ? won : win.revealing) && motionEnabled
  useEffect(() => {
    if (!revealing) return
    const timeout = setTimeout(() => setWin(current => ({ ...current, revealing: false })), 3000)
    return () => clearTimeout(timeout)
  }, [card, won, revealing])
  const [flipped, setFlipped] = useState(false)
  const timer = useRef()
  useEffect(() => {
    setFlipped(false)
    return () => clearTimeout(timer.current)
  }, [card, won])
  const faceUp = !won || cardsVisible || flipped || revealing
  const face = faceUp ? card : "back"
  const [flip, flipDone] = useFlip(face, motionEnabled)
  const playable = !won && legal && Boolean(action && actionName)
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
      className={`playing-card${playable ? " playable" : ""}${!legal ? " illegal-play" : ""}${won && faceUp ? " won-visible" : ""}${revealing ? " trick-winner" : ""}`}
      disabled={!playable && !canFlip}
      onClick={click}
      aria-label={
        playable
          ? `Valitse ${card}`
          : canFlip && !faceUp
            ? "Näytä voittokortti"
            : faceUp
              ? // a revealed winning card is raised; say it in the label too
                `${card}${won ? ", voitettu tikki" : ""}`
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
    </button>
  )
}
export default Card
