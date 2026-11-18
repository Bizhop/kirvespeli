import type { AuthStatus } from "../../auth/session.ts"
import type { GameOut } from "../../types/kirves.ts"
import type React from "react"
import { Navigate } from "react-router"
import { Tooltip, IconButton, Box, Grid, Button } from "@mui/material"
import RefreshIcon from "@mui/icons-material/Refresh"

import { useGames } from "../../game/hooks.ts"
import RequestStatus, { type RequestStatusProps } from "./RequestStatus.tsx"
import RenderGame from "./RenderGame.tsx"
import KirvesGames from "./KirvesGames.tsx"
import Log from "./Log.tsx"
import MessagesButton from "./MessagesButton.tsx"

export const KirvesContainer = (
  props: React.ComponentProps<typeof KirvesGames> &
    React.ComponentProps<typeof Log> &
    RequestStatusProps & {
      init: () => void
      getGames: () => void
      replay?: GameOut | null
      logVisible?: boolean
      authStatus?: AuthStatus
    },
) => {
  return (
    <Box sx={{ flexGrow: 1 }}>
      <h1>Kirves</h1>
      <Grid container spacing={1}>
        <Grid
          size={{
            md: 3,
          }}
        >
          <Button variant="contained" disabled={props.pending} onClick={() => props.init()}>
            Aloita uusi peli
          </Button>
        </Grid>
        <Grid
          size={{
            md: 3,
          }}
        >
          <Tooltip title="Hae pelit">
            <IconButton disabled={props.loading || props.pending} onClick={() => props.getGames()}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Grid>
      </Grid>
      <RequestStatus {...props} />
      <KirvesGames {...props} />
      {props.logVisible && <Log {...props} />}
      {props.replay && (
        <Box className="kirves-container">
          <h2 className="game-heading">
            Tilanne
            <MessagesButton key={props.replay.id} messages={props.replay.messages ?? []} />
          </h2>
          <RenderGame
            game={props.replay}
            isReplay={true}
            action={() => {}}
            showAllCards={() => {}}
            cardsVisible={true}
          />
        </Box>
      )}
      {props.authStatus === "anonymous" && <Navigate to="/" />}
    </Box>
  )
}

const KirvesContainerWithState = () => {
  const state = useGames()
  if (state.authStatus === "restoring") return <p role="status">Palautetaan istuntoa…</p>
  if (state.authStatus === "anonymous") return <Navigate to="/" />
  return <KirvesContainer key={state.sessionKey} {...state} />
}

export default KirvesContainerWithState
