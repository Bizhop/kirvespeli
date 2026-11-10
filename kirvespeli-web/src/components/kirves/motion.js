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
}

export const prefersReducedMotion = () =>
  typeof window !== "undefined" &&
  Boolean(window.matchMedia?.("(prefers-reduced-motion: reduce)").matches)
