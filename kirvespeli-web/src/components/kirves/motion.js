// Shared card motion timing. CSS-only effects (flip) use matching values in style.css.
export const MOTION = {
  moveMs: 260,
  flightMs: 320,
  fadeMs: 200,
  dealMs: 300,
  // per-card deal delay; the whole stagger stays within dealStaggerTotalMs
  dealStaggerMs: 70,
  dealStaggerTotalMs: 300,
  easing: "cubic-bezier(0.2, 0.7, 0.2, 1)",
  // a trick's winning card stays face up this long
  trickRevealMs: 3000,
  // score sheet: one pen stroke, a stick or axe mark, the pause between players
  scoreStrokeMs: 380,
  scoreLossMs: 600,
  scoreStaggerMs: 150,
  // score note: opening/closing transition (matches style.css) and how long an automatically
  // opened note stays open after the last mark
  slipMs: 280,
  slipHoldMs: 2500,
}

export const prefersReducedMotion = () =>
  typeof window !== "undefined" &&
  Boolean(window.matchMedia?.("(prefers-reduced-motion: reduce)").matches)
