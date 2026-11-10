import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useLayoutEffect,
  useMemo,
  useRef,
} from "react"
import { planCardMotion, DECK_KEY } from "./cardMotion"
import { MOTION, prefersReducedMotion } from "./motion"

// Table-owned card motion. Components register elements under the keys from cardMotion.js;
// only the own hand, played-card rows, deck, cut and trump cards are registered. Seat-hand
// backs never are, so nothing about another player's hand can be measured or animated.
//
// After each confirmed snapshot the registered elements are measured relative to the table.
// Elements that keep their key move from their previous position (FLIP); new and removed
// elements fade, the own played card flies from the hand to its row, and a deal comes from
// the deck. Redux state is never delayed: motion only decorates the already rendered state.

const MotionContext = createContext(null)

// Returns key => ref callback, or undefined outside a CardMotion table.
export const useMotionRef = () => useContext(MotionContext)

const center = rect => [rect.x + rect.w / 2, rect.y + rect.h / 2]

const baseTransform = el => {
  const value = getComputedStyle(el).transform
  return value && value !== "none" ? value : ""
}

const CardMotion = ({ game, enabled = true, resetKey, className, children }) => {
  const rootRef = useRef(null)
  const overlayRef = useRef(null)
  const elements = useRef(new Map())
  const refs = useRef(new Map())
  const cache = useRef(new Map())
  const previous = useRef(null)
  const running = useRef(new Set())
  const resetPending = useRef(false)

  const ref = useCallback(key => {
    let callback = refs.current.get(key)
    if (!callback) {
      callback = el => {
        if (!el) return
        elements.current.set(key, el)
        return () => {
          if (elements.current.get(key) === el) elements.current.delete(key)
        }
      }
      refs.current.set(key, callback)
    }
    return callback
  }, [])

  const settle = useCallback(() => {
    for (const entry of running.current) {
      entry.animation.cancel()
      entry.cleanup?.()
    }
    running.current.clear()
  }, [])

  const measure = useCallback(() => {
    const root = rootRef.current
    const measured = new Map()
    if (!root) return measured
    const origin = root.getBoundingClientRect()
    for (const [key, el] of elements.current) {
      const r = el.getBoundingClientRect()
      if (!r.width && !r.height) continue
      measured.set(key, {
        el,
        rect: { x: r.left - origin.left, y: r.top - origin.top, w: r.width, h: r.height },
      })
    }
    return measured
  }, [])

  const run = useCallback((el, keyframes, options, cleanup) => {
    if (!el?.animate) return cleanup?.()
    const animation = el.animate(keyframes, { easing: MOTION.easing, ...options })
    const entry = { animation, cleanup }
    running.current.add(entry)
    animation.finished
      .then(() => {
        if (!running.current.delete(entry)) return
        // keep the final frame until the cleanup runs, then drop the animation
        entry.cleanup?.()
        animation.cancel()
      })
      .catch(() => {})
  }, [])

  // Decorative copy of a card in the overlay, positioned at `rect`.
  const ghost = useCallback((source, rect) => {
    const overlay = overlayRef.current
    if (!overlay) return null
    const copy = source.cloneNode(true)
    copy.removeAttribute("style")
    Object.assign(copy.style, {
      position: "absolute",
      left: `${rect.x}px`,
      top: `${rect.y}px`,
      width: `${rect.w}px`,
      height: `${rect.h}px`,
      margin: "0",
      visibility: "visible",
    })
    copy.style.setProperty("--card-w", `${rect.w}px`)
    overlay.appendChild(copy)
    return copy
  }, [])

  const animate = useCallback(
    (plan, before, after) => {
      const handled = new Set()

      // own card played: fly a copy from the hand slot to the played row
      for (const { from, to } of plan.own.flights) {
        const start = before.get(from)
        const end = after.get(to)
        handled.add(to)
        if (!start || !end) continue
        const copy = ghost(end.el, end.rect)
        if (!copy) continue
        const [sx, sy] = center(start.rect)
        const [ex, ey] = center(end.rect)
        end.el.style.visibility = "hidden"
        run(
          copy,
          [
            {
              transform: `translate(${sx - ex}px, ${sy - ey}px) scale(${start.rect.w / end.rect.w})`,
            },
            { transform: "none" },
          ],
          { duration: MOTION.flightMs },
          () => {
            copy.remove()
            end.el.style.visibility = ""
          },
        )
      }

      // deal: own cards arrive from the deck in display order with a bounded stagger
      if (plan.own.deal) {
        const deck = after.get(DECK_KEY) ?? before.get(DECK_KEY)
        const dealt = plan.own.entering
          .map(key => after.get(key))
          .filter(Boolean)
          .sort((a, b) => a.rect.x - b.rect.x)
        const step =
          dealt.length > 1
            ? Math.min(MOTION.dealStaggerMs, MOTION.dealStaggerTotalMs / (dealt.length - 1))
            : 0
        dealt.forEach(({ el, rect }, i) => {
          const from = deck
            ? (() => {
                const [dx, dy] = center(deck.rect)
                const [cx, cy] = center(rect)
                return `translate(${dx - cx}px, ${dy - cy}px) scale(${deck.rect.w / rect.w})`
              })()
            : "translateY(16px)"
          run(
            el,
            [
              { transform: from, opacity: 0 },
              { opacity: 1, offset: 0.25 },
              { transform: "none", opacity: 1 },
            ],
            { duration: MOTION.dealMs, delay: i * step, fill: "backwards" },
          )
        })
        plan.own.entering.forEach(key => handled.add(key))
      }

      // other new cards fade in where they are
      const fadeIn = [
        ...(plan.own.deal ? [] : plan.own.entering),
        ...plan.public.plays,
        ...plan.public.entering,
      ]
      for (const key of fadeIn) {
        const entry = after.get(key)
        handled.add(key)
        if (!entry) continue
        const base = baseTransform(entry.el)
        const opacity = getComputedStyle(entry.el).opacity
        run(
          entry.el,
          [
            { opacity: 0, transform: `${base} scale(0.85)` },
            { opacity, transform: base || "none" },
          ],
          { duration: MOTION.fadeMs },
        )
      }

      // removed cards (discard, cleared table, replaced cut cards) fade out from a copy
      for (const key of [...plan.own.leaving, ...plan.public.cleared, ...plan.public.leaving]) {
        const entry = before.get(key)
        if (!entry || after.has(key)) continue
        const copy = ghost(entry.el, entry.rect)
        if (!copy) continue
        run(
          copy,
          [
            { opacity: 1, transform: "none" },
            { opacity: 0, transform: "translateY(8px) scale(0.95)" },
          ],
          { duration: MOTION.fadeMs },
          () => copy.remove(),
        )
      }

      // everything else that kept its identity moves from its previous position
      for (const [key, { el, rect }] of after) {
        const old = before.get(key)
        if (!old || handled.has(key)) continue
        const [ox, oy] = center(old.rect)
        const [nx, ny] = center(rect)
        const dx = ox - nx
        const dy = oy - ny
        if (Math.abs(dx) < 0.5 && Math.abs(dy) < 0.5) continue
        const base = baseTransform(el)
        run(
          el,
          [{ transform: `translate(${dx}px, ${dy}px) ${base}` }, { transform: base || "none" }],
          { duration: MOTION.moveMs },
        )
      }
    },
    [ghost, run],
  )

  // A reset (reconnect, resync) makes the next snapshot render without motion.
  const firstReset = useRef(true)
  useLayoutEffect(() => {
    if (firstReset.current) firstReset.current = false
    else resetPending.current = true
  }, [resetKey])

  useLayoutEffect(() => {
    const prev = previous.current
    previous.current = game
    settle()
    const before = cache.current
    const after = measure()
    cache.current = after
    const skip = resetPending.current || !enabled || prefersReducedMotion() || document.hidden
    resetPending.current = false
    if (skip) return
    const plan = planCardMotion(prev, game)
    if (!plan.snap) animate(plan, before, after)
  }, [game, enabled, settle, measure, animate])

  // Width changes (resize, sidebar layout) invalidate positions: settle and remeasure.
  useEffect(() => {
    const root = rootRef.current
    let width = root?.getBoundingClientRect().width
    const refresh = () => {
      settle()
      cache.current = measure()
    }
    const observer =
      typeof ResizeObserver !== "undefined" &&
      new ResizeObserver(([entry]) => {
        if (entry.contentRect.width === width) return
        width = entry.contentRect.width
        refresh()
      })
    if (observer && root) observer.observe(root)
    const media = window.matchMedia?.("(prefers-reduced-motion: reduce)")
    media?.addEventListener?.("change", settle)
    document.addEventListener("visibilitychange", settle)
    return () => {
      observer?.disconnect()
      media?.removeEventListener?.("change", settle)
      document.removeEventListener("visibilitychange", settle)
      settle()
    }
  }, [settle, measure])

  const context = useMemo(() => ref, [ref])
  return (
    <div className={className} ref={rootRef}>
      <MotionContext.Provider value={context}>{children}</MotionContext.Provider>
      <div className="card-motion-overlay" ref={overlayRef} aria-hidden="true" inert />
    </div>
  )
}
export default CardMotion
