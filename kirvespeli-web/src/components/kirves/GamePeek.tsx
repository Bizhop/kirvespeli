import type { GameBrief, GameOut } from "../../types/kirves.ts"
import { useCallback, useEffect, useRef, useState } from "react"
import { NavLink } from "react-router"
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Tooltip,
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
  return (
    <Dialog open onClose={onClose} maxWidth="md" fullWidth aria-labelledby="game-peek-title">
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
        <Button onClick={onClose}>Sulje</Button>
        <Button component={NavLink} to={`/kirves/${brief.id}`} variant="contained">
          Avaa peli
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default GamePeek
