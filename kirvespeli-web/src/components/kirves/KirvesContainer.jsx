import React, { useEffect } from "react"
import { connect } from "react-redux"
import { Navigate } from "react-router"
import { path, pathOr, pick } from "ramda"
import { Tooltip, IconButton, Box, Grid, Button } from "@mui/material"
import RefreshIcon from "@mui/icons-material/Refresh"

import { useAuth } from "../../auth/useAuth.ts"
import {
  init,
  getGames,
  deleteGame,
  getLog,
  getReplay,
  restoreGame,
  peekGame,
} from "./kirvesActions"
import RenderGame from "./RenderGame"
import KirvesGames from "./KirvesGames"
import Log from "./Log"
import MessagesButton from "./MessagesButton"

export const KirvesContainer = props => {
  useEffect(() => {
    if (!props.gamesFetched) {
      props.getGames()
    }
  }, [props.gamesFetched])
  return (
    <Box sx={{ flexGrow: 1 }}>
      <h1>Kirves</h1>
      <Grid container spacing={1}>
        <Grid
          size={{
            md: 3,
          }}
        >
          <Button variant="contained" onClick={() => props.init()}>
            Aloita uusi peli
          </Button>
        </Grid>
        <Grid
          size={{
            md: 3,
          }}
        >
          <Tooltip title="Hae pelit">
            <IconButton onClick={() => props.getGames()}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Grid>
      </Grid>
      <KirvesGames {...pick(["games", "user", "getLog", "deleteGame", "fetchGame"], props)} />
      {props.logVisible && (
        <Log
          {...pick(["logId", "logItems", "selectedLogIndex", "getReplay", "restoreGame"], props)}
        />
      )}
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

const mapStateToProps = state => ({
  games: path(["kirves", "games"], state),
  logItems: pathOr([], ["kirves", "logItems"], state),
  logVisible: path(["kirves", "logVisible"], state),
  logId: path(["kirves", "logId"], state),
  replay: path(["kirves", "replay"], state),
  selectedLogIndex: pathOr(0, ["kirves", "selectedLogIndex"], state),
  gamesFetched: pathOr(false, ["kirves", "gamesFetched"], state),
})

const mapDispatchToProps = dispatch => ({
  init: () => dispatch(init()),
  deleteGame: gameId => dispatch(deleteGame(gameId)),
  getLog: params => dispatch(getLog(params)),
  getReplay: params => dispatch(getReplay(params)),
  restoreGame: params => dispatch(restoreGame(params)),
  getGames: () => dispatch(getGames()),
  // resolves with the game snapshot, rejects when the request fails
  fetchGame: gameId =>
    dispatch(peekGame(gameId)).then(result =>
      result.error ? Promise.reject(result.error) : result.payload.data,
    ),
})

const ConnectedKirvesContainer = connect(mapStateToProps, mapDispatchToProps)(KirvesContainer)

const KirvesContainerWithAuth = () => {
  const { user, status } = useAuth()
  return <ConnectedKirvesContainer user={user} authStatus={status} />
}

export default KirvesContainerWithAuth
