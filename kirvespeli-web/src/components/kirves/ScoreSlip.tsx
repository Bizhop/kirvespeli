import type { GameOut } from "../../types/kirves.ts"
import type { ScoreSchedule } from "./scoreChanges.ts"
import { useEffect, useId, useRef, useState } from "react"
import { ClickAwayListener, IconButton, Tooltip } from "@mui/material"
import CloseIcon from "@mui/icons-material/Close"

import ScoreCard from "./ScoreCard.tsx"
import ScoreMark from "./ScoreMark.tsx"
import { scoreChanges, scoreSchedule } from "./scoreChanges.ts"
import { MOTION } from "./motion.ts"

// The score sheet as a paper note on the table. Tapping the note opens it over the table;
// tapping it again, tapping outside it or Esc closes it.
//
// When a confirmed snapshot adds points, the new marks are drawn in after the last trick has
// been shown (MOTION.trickRevealMs): the note opens by itself, the marks are drawn one by one,
// and it closes again after MOTION.slipHoldMs, unless the viewer has opened or touched it.
// Like card motion, this only decorates the rendered snapshot: the new marks are in the DOM at
// once, hidden until drawn. A resync (resetKey), another game or a replay shows them as is.

let nextAnimationId = 1

const useScoreDrawing = (game: GameOut, enabled: boolean, resetKey?: number) => {
  const [state, setState] = useState<{
    game: GameOut
    resetKey?: number
    drawing: { id: number; schedule: ScoreSchedule } | null
  }>({ game, resetKey, drawing: null })
  let { drawing } = state
  if (state.game !== game || state.resetKey !== resetKey) {
    const changes = enabled && state.resetKey === resetKey ? scoreChanges(state.game, game) : null
    if (changes) {
      drawing = {
        id: nextAnimationId++,
        schedule: scoreSchedule(changes, {
          startMs: MOTION.trickRevealMs,
          strokeMs: MOTION.scoreStrokeMs,
          lossMs: MOTION.scoreLossMs,
          staggerMs: MOTION.scoreStaggerMs,
        }),
      }
    } else if (!enabled || state.resetKey !== resetKey || state.game?.id !== game?.id) {
      drawing = null
    }
    // other snapshots keep a drawing in progress: its marks are already timed
    setState({ game, resetKey, drawing })
  }
  const id = drawing?.id
  useEffect(() => {
    if (!id || !drawing) return
    const done = setTimeout(
      () =>
        setState(current => (current.drawing?.id === id ? { ...current, drawing: null } : current)),
      drawing.schedule.endMs + MOTION.slipHoldMs + MOTION.slipMs,
    )
    return () => clearTimeout(done)
  }, [id])
  return drawing
}

const ScoreSlip = ({
  game,
  animate = true,
  resetKey,
}: {
  game: GameOut
  animate?: boolean
  resetKey?: number
}) => {
  const drawing = useScoreDrawing(game, animate, resetKey)
  // false, "auto" (opened for drawing, closes by itself) or "manual"
  const [open, setOpen] = useState<false | "auto" | "manual">(false)
  const panelId = useId()
  const toggleRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!drawing) return
    const { startMs, endMs } = drawing.schedule
    const show = setTimeout(
      () => setOpen(current => current || "auto"),
      Math.max(0, startMs - MOTION.slipMs),
    )
    const hide = setTimeout(
      () => setOpen(current => (current === "auto" ? false : current)),
      endMs + MOTION.slipHoldMs,
    )
    return () => {
      clearTimeout(show)
      clearTimeout(hide)
    }
  }, [drawing?.id])

  useEffect(() => {
    if (!open) return
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false)
        toggleRef.current?.focus()
      }
    }
    document.addEventListener("keydown", onKey)
    return () => document.removeEventListener("keydown", onKey)
  }, [open])

  if (!game.scores?.length) return null
  return (
    <ClickAwayListener onClickAway={() => setOpen(false)}>
      <div className={`score-slip${open ? " score-slip-open" : ""}`}>
        <Tooltip title="Pisteet">
          <button
            type="button"
            ref={toggleRef}
            className="score-slip-note"
            aria-label="Pisteet"
            aria-expanded={Boolean(open)}
            aria-controls={panelId}
            onClick={() => setOpen(current => (current ? false : "manual"))}
          >
            <ScoreMark score={3} seed="note" size={26} />
          </button>
        </Tooltip>
        <div
          id={panelId}
          className="score-slip-paper"
          role="region"
          aria-label="Pistetaulukko"
          aria-hidden={!open}
          inert={!open || undefined}
          onPointerDown={() => setOpen("manual")}
        >
          <div className="score-slip-title">
            <h2>Pisteet</h2>
            <IconButton size="small" aria-label="Sulje pisteet" onClick={() => setOpen(false)}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </div>
          <ScoreCard game={game} schedule={drawing?.schedule} />
        </div>
      </div>
    </ClickAwayListener>
  )
}

export default ScoreSlip
