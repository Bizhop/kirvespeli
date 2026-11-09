import React from "react"
import { connect } from "react-redux"
import { Navigate, useLocation } from "react-router-dom"
import { path, includes, pathOr, omit } from "ramda"
import { Grid, Box, Button } from "@mui/material"
import SockJsClient from "react-stomp"

import { getGame, joinGame, action, showAllCards } from "./kirvesActions"
import RenderGame from "./RenderGame"
import ScoreCard from "./ScoreCard"
import ActionButton from "./ActionButton"
import Messages from "./Messages"
import SuitSelector from "./SuitSelector"

const getGameIdAndGame = getGame => {
  const location = useLocation()
  var id = location.pathname.split("/").pop()
  getGame(id)
}

const makeButton = props => {
  const buttonProps = omit(["type", "label"], props)
  switch (props.type) {
    case "button":
      return (
        <Button {...buttonProps} variant="contained">
          {props.label}
        </Button>
      )
    case "action":
      return <ActionButton {...buttonProps} label={props.label} />
    default:
      return null
  }
}

const ButtonWrapper = props => (
  <Grid item md={2}>
    {makeButton(props)}
  </Grid>
)

export const KirvesGame = ({
  user,
  game,
  cardsVisible,
  join,
  refresh,
  action,
  showAllCards,
  getGame,
  socketDisabled,
}) => (
  <div>
    {game ? (
      <Box sx={{ flexGrow: 1 }} className="kirves-container">
        {!socketDisabled && (
          <SockJsClient
            url={process.env.WEB_SOCKET_URL}
            topics={["/topic/refresh"]}
            onMessage={msg => {
              if (msg == game.id) {
                refresh(game.id)
              }
            }}
          />
        )}
        <h1>Kirves (Peli {game.id})</h1>
        <Grid container spacing={1}>
          {game.canJoin && (
            <ButtonWrapper type="button" label="Liity peliin" onClick={() => join(game.id)} />
          )}
          {includes("DEAL", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="DEAL"
              gameId={game.id}
              label="Jaa"
            />
          )}
          {includes("FOLD", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="FOLD"
              gameId={game.id}
              label="Mene pakkaan"
            />
          )}
          {includes("CUT", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="CUT"
              gameId={game.id}
              label="Nosta"
            />
          )}
          {includes("CUT", game.myAvailableActions) && game.canDeclineCut && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="CUT"
              gameId={game.id}
              declineCut="true"
              label="Älä nosta"
            />
          )}
          {includes("ACE_OR_TWO_DECISION", game.myAvailableActions) && (
            <ButtonWrapper
              action={action}
              actionName="ACE_OR_TWO_DECISION"
              gameId={game.id}
              keepExtraCard="true"
              label="Pidä"
            />
          )}
          {includes("ACE_OR_TWO_DECISION", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="ACE_OR_TWO_DECISION"
              gameId={game.id}
              keepExtraCard="false"
              label="Hylkää"
            />
          )}
          {includes("SPEAK", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="SPEAK"
              gameId={game.id}
              speak="KEEP"
              label="Päältä"
            />
          )}
          {includes("SPEAK", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="SPEAK"
              gameId={game.id}
              speak="CHANGE"
              label="Värjäisin"
            />
          )}
          {includes("SPEAK", game.myAvailableActions) && (
            <ButtonWrapper
              type="action"
              action={action}
              actionName="SPEAK"
              gameId={game.id}
              speak="PASS"
              label="Viitenä"
            />
          )}
          {includes("SPEAK_SUIT", game.myAvailableActions) && (
            <SuitSelector
              action={action}
              actionName="SPEAK_SUIT"
              gameId={game.id}
              currentTrump={game.trump}
            />
          )}
          <Grid container spacing={1}>
            <Grid item md display="flex" justifyContent="flex-end">
              <Messages messages={pathOr([], ["messages"], game)} />
            </Grid>
          </Grid>
        </Grid>
        <RenderGame
          game={game}
          isReplay={false}
          action={action}
          showAllCards={showAllCards}
          cardsVisible={cardsVisible}
        />
        <Grid container spacing={1}>
          <Grid item md display="flex" justifyContent="flex-end">
            <ScoreCard game={game} />
          </Grid>
        </Grid>
      </Box>
    ) : (
      getGameIdAndGame(getGame) && null
    )}
    {!user.email && <Navigate to="/" />}
  </div>
)

const mapStateToProps = state => ({
  user: path(["user"], state),
  game: path(["kirves", "game"], state),
  cardsVisible: path(["kirves", "cardsVisible"], state),
})

const mapDispatchToProps = dispatch => ({
  join: gameId => dispatch(joinGame(gameId)),
  refresh: gameId => dispatch(getGame(gameId)),
  action: params => dispatch(action(params)),
  showAllCards: () => dispatch(showAllCards()),
  getGame: gameId => dispatch(getGame(gameId)),
})

export default connect(mapStateToProps, mapDispatchToProps)(KirvesGame)
