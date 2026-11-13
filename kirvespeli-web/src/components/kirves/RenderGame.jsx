import React, { useLayoutEffect, useRef } from "react"
import Table from "./Table"
import ScoreCard from "./ScoreCard"
import Messages from "./Messages"

// Keeps --game-top at the element's distance from the document top, so the fit layout can
// size the table to the rest of the viewport. Anything above it (site header, game heading,
// a wrapping title) is observed instead of being budgeted by hand.
const useGameTop = enabled => {
  const ref = useRef(null)
  useLayoutEffect(() => {
    const el = ref.current
    if (!enabled || !el) return
    const update = () => {
      const top = Math.ceil(el.getBoundingClientRect().top + window.scrollY)
      el.style.setProperty("--game-top", `${top}px`)
    }
    update()
    const observer = typeof ResizeObserver !== "undefined" && new ResizeObserver(update)
    for (let node = el; observer && node && node !== document.body; node = node.parentElement) {
      for (let above = node.previousElementSibling; above; above = above.previousElementSibling) {
        observer.observe(above)
      }
    }
    window.addEventListener("resize", update)
    return () => {
      observer?.disconnect()
      window.removeEventListener("resize", update)
      el.style.removeProperty("--game-top")
    }
  }, [enabled])
  return ref
}

// Both live games and log replays use the same seat order and table rendering.
export const RenderGame = ({ fitViewport = false, ...props }) => {
  const ref = useGameTop(fitViewport)
  return (
    <div className={`game-layout${fitViewport ? " fit-viewport" : ""}`} ref={ref}>
      <Table {...props} />
      <aside className="game-sidebar">
        <h2>Pisteet</h2>
        <ScoreCard game={props.game} />
        <div className="messages-panel">
          <Messages messages={props.game.messages ?? []} />
        </div>
      </aside>
    </div>
  )
}
export default RenderGame
