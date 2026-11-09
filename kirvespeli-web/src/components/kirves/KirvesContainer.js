import React from "react"
import { connect } from "react-redux"
import { Navigate } from "react-router-dom"
import { path, pathOr, pick } from "ramda"
import { Tooltip, IconButton, Box, Grid, Button, Alert } from "@mui/material"
import RefreshIcon from "@mui/icons-material/Refresh"

import { init, getGames, deleteGame, getLog, getReplay, restoreGame } from "./kirvesActions"
import RenderGame from "./RenderGame"
import KirvesGames from "./KirvesGames"
import Log from "./Log"

export const KirvesContainer = props => (
  <Box sx={{ flexGrow: 1 }}>
    <h1>Kirves</h1>
    <Grid container spacing={1}>
      <Grid item md={3}>
        <Button variant="contained" onClick={() => props.init()}>
          Aloita uusi peli
        </Button>
      </Grid>
      <Grid item md={3}>
        <Tooltip title="Hae pelit">
          <IconButton onClick={() => props.getGames()}>
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Grid>
    </Grid>
    <KirvesGames {...pick(["games", "user", "getLog", "deleteGame"], props)} />
    {props.logVisible && (
      <Log
        {...pick(["logId", "logItems", "selectedLogIndex", "getReplay", "restoreGame"], props)}
      />
    )}
    {props.replay && (
      <Box className="kirves-container">
        <RenderGame
          game={props.replay}
          isReplay={true}
          action={() => {}}
          showAllCards={() => {}}
          cardsVisible={true}
        />
      </Box>
    )}
    {!props.user.email && <Navigate to="/" />}
    {!props.gamesFetched && props.getGames() && null}
    {props.error && <Alert severity="error">{props.error}</Alert>}
  </Box>
)

const mapStateToProps = state => ({
  user: path(["user"], state),
  error: path(["user", "error"], state),
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
})

export default connect(mapStateToProps, mapDispatchToProps)(KirvesContainer)
