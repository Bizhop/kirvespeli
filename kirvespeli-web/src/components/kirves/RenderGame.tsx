import type { TableProps } from "./props.ts"
import { useLayoutEffect, useRef } from "react"
import Table from "./Table.tsx"

// Keeps --game-top at the element's distance from the document top, so the fit layout can
// size the table to the rest of the viewport. Anything above it (site header, game heading,
// a wrapping title) is observed instead of being budgeted by hand.
const useGameTop = (enabled: boolean) => {
  const ref = useRef<HTMLDivElement>(null)
  useLayoutEffect(() => {
    const el = ref.current
    if (!enabled || !el) return
    const update = () => {
      const top = Math.ceil(el.getBoundingClientRect().top + window.scrollY)
      el.style.setProperty("--game-top", `${top}px`)
    }
    update()
    const observer = typeof ResizeObserver !== "undefined" ? new ResizeObserver(update) : undefined
    for (
      let node: HTMLElement | null = el;
      observer && node && node !== document.body;
      node = node.parentElement
    ) {
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

// Both live games and log replays use the same seat order and table rendering. The score
// sheet is a note on the table (ScoreSlip); messages are behind the title (MessagesButton).
export const RenderGame = ({
  fitViewport = false,
  ...props
}: TableProps & { fitViewport?: boolean }) => {
  const ref = useGameTop(fitViewport)
  return (
    <div className={`game-layout${fitViewport ? " fit-viewport" : ""}`} ref={ref}>
      <Table {...props} />
    </div>
  )
}
export default RenderGame
