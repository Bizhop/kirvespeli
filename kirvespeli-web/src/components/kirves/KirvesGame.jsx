import React, { useCallback, useEffect, useState } from "react"
import { connect } from "react-redux"
import { Navigate, useParams } from "react-router"
import { path } from "ramda"
import { Box } from "@mui/material"

import { useAuth } from "../../auth/useAuth.ts"
import { getGame, joinGame, action, showAllCards } from "./kirvesActions"
import RenderGame from "./RenderGame"
import Icon from "../shared/Icon"
import MessagesButton from "./MessagesButton"
import useGameRefresh from "./useGameRefresh"

export const KirvesGame = ({
  authStatus,
  game,
  cardsVisible,
  join,
  refresh,
  action,
  showAllCards,
  getGame,
  socketDisabled,
}) => {
  const { id } = useParams()
  useEffect(() => {
    if (id && String(game?.id) !== id) {
      getGame(id)
    }
  }, [id, game?.id])
  // a resync renders the next snapshot without card motion
  const [resyncKey, setResyncKey] = useState(0)
  const resync = useCallback(
    gameId => {
      setResyncKey(key => key + 1)
      refresh(gameId)
    },
    [refresh],
  )
  useGameRefresh(game?.id, refresh, !socketDisabled, resync)
  return (
    <div>
      {game && (
        <Box sx={{ flexGrow: 1 }} className="kirves-container">
          <h1 className="game-heading">
            <Icon name="axe" size={44} />
            Kirves (Peli {game.id})
            <MessagesButton key={game.id} messages={game.messages ?? []} />
          </h1>
          <RenderGame
            game={game}
            isReplay={false}
            fitViewport
            action={action}
            join={join}
            showAllCards={showAllCards}
            cardsVisible={cardsVisible}
            motionResetKey={resyncKey}
          />
        </Box>
      )}
      {authStatus === "anonymous" && <Navigate to="/" />}
    </div>
  )
}

const mapStateToProps = state => ({
  game: path(["kirves", "game"], state),
  cardsVisible: path(["kirves", "cardsVisible"], state),
})

const mapDispatchToProps = dispatch => ({
  join: gameId => dispatch(joinGame(gameId)).then(() => dispatch(getGame(gameId))),
  refresh: gameId => dispatch(getGame(gameId)),
  action: params => dispatch(action(params)),
  showAllCards: () => dispatch(showAllCards()),
  getGame: gameId => dispatch(getGame(gameId)),
})

const ConnectedKirvesGame = connect(mapStateToProps, mapDispatchToProps)(KirvesGame)

const KirvesGameWithAuth = () => {
  const { status } = useAuth()
  return <ConnectedKirvesGame authStatus={status} />
}

export default KirvesGameWithAuth
