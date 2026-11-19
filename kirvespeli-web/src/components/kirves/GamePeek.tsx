import type { GameBrief, GameOut } from "../../types/kirves.ts"
import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import { NavLink } from "react-router"
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  ThemeProvider,
  Tooltip,
  type Theme,
} from "@mui/material"
import RefreshIcon from "@mui/icons-material/Refresh"

import RenderGame from "./RenderGame.tsx"
import MessagesButton from "./MessagesButton.tsx"
import { playerName } from "../shared/playerName.ts"

// A read-only look at a game from the game list. The snapshot is kept here, independently of live play,
// so peeking never replaces the game open in the game view. Hands stay hidden: the backend
// leaves them out for anyone not seated, and the viewer's own hand is not shown here either.
const GamePeek = ({
  brief,
  fetchGame,
  onClose,
}: {
  brief: GameBrief
  fetchGame: (id: number, signal?: AbortSignal) => Promise<GameOut>
  onClose: () => void
}) => {
  const [game, setGame] = useState<GameOut | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const request = useRef<AbortController | null>(null)
  const load = useCallback(() => {
    request.current?.abort()
    const controller = new AbortController()
    request.current = controller
    setLoading(true)
    setError(null)
    Promise.resolve()
      .then(() => fetchGame(brief.id, controller.signal))
      .then(
        snapshot => {
          if (!controller.signal.aborted) setGame(snapshot)
        },
        (error: unknown) => {
          if (!controller.signal.aborted)
            setError(error instanceof Error ? error.message : "Pelin haku epäonnistui")
        },
      )
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
  }, [brief.id, fetchGame])
  useEffect(() => {
    setGame(null)
    load()
    return () => request.current?.abort()
  }, [load])
  const dialog = useRef<HTMLDialogElement>(null)
  // MUI tooltips portal into <body>, which sits under the modal in the top layer and is inert:
  // render them inside the dialog instead
  const [container, setContainer] = useState<HTMLDialogElement | null>(null)
  const onCloseRef = useRef(onClose)
  useLayoutEffect(() => {
    onCloseRef.current = onClose
  })
  useLayoutEffect(() => {
    const element = dialog.current
    if (!element) return
    setContainer(element)
    // no close() in the cleanup: StrictMode's remount would then report a close to the parent
    if (!element.open) element.showModal()
    const close = () => onCloseRef.current()
    element.addEventListener("close", close)
    const cleanup = closedByFallback(element)
    return () => {
      element.removeEventListener("close", close)
      cleanup?.()
    }
  }, [])
  return (
    <dialog ref={dialog} closedby="any" aria-labelledby="game-peek-title" className="game-peek">
      <ThemeProvider theme={(outer: Theme) => tooltipsIn(outer, container)}>
        <DialogTitle id="game-peek-title" className="game-heading" sx={{ m: 0 }}>
          Peli {brief.id}
          <Box component="span" sx={{ fontSize: "1rem", fontWeight: "normal" }}>
            Aloittaja: {playerName(brief.admin)}
          </Box>
          {game && <MessagesButton key={game.id} messages={game.messages ?? []} />}
          <Tooltip title="Päivitä">
            <span style={{ marginLeft: "auto" }}>
              <IconButton onClick={load} disabled={loading} aria-label="Päivitä tilanne">
                <RefreshIcon />
              </IconButton>
            </span>
          </Tooltip>
        </DialogTitle>
        <DialogContent dividers sx={{ px: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}
          {!game && !error && (
            <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
              <CircularProgress />
            </Box>
          )}
          {game && (
            <RenderGame
              game={game}
              isReplay={true}
              action={() => {}}
              showAllCards={() => {}}
              cardsVisible={false}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => dialog.current?.close()}>Sulje</Button>
          <Button component={NavLink} to={`/kirves/${brief.id}`} variant="contained">
            Avaa peli
          </Button>
        </DialogActions>
      </ThemeProvider>
    </dialog>
  )
}

const tooltipsIn = (outer: Theme, container: HTMLElement | null): Theme => ({
  ...outer,
  components: {
    ...outer.components,
    MuiTooltip: {
      ...outer.components?.MuiTooltip,
      defaultProps: {
        ...outer.components?.MuiTooltip?.defaultProps,
        slotProps: { popper: { container } },
      },
    },
  },
})

// Light dismiss where closedby is missing (Safari). The press and the release must both land
// outside the dialog, so a text selection dragged out onto the backdrop doesn't close it.
// Popovers opened from inside the dialog (messages) are its descendants but drawn outside it.
const closedByFallback = (dialog: HTMLDialogElement) => {
  if ("closedBy" in HTMLDialogElement.prototype) return
  const outside = (event: PointerEvent) => {
    if (event.target instanceof Element && event.target.closest(":popover-open")) return false
    const rect = dialog.getBoundingClientRect()
    return (
      event.clientX < rect.left ||
      event.clientX > rect.right ||
      event.clientY < rect.top ||
      event.clientY > rect.bottom
    )
  }
  let pressedOutside = false
  const down = (event: PointerEvent) => {
    pressedOutside = outside(event)
  }
  const up = (event: PointerEvent) => {
    if (pressedOutside && outside(event)) dialog.close()
  }
  dialog.addEventListener("pointerdown", down)
  dialog.addEventListener("pointerup", up)
  return () => {
    dialog.removeEventListener("pointerdown", down)
    dialog.removeEventListener("pointerup", up)
  }
}

export default GamePeek
