import React, { useEffect } from "react"
import { connect } from "react-redux"
import { Navigate, useParams } from "react-router"
import { path } from "ramda"
import { Box } from "@mui/material"

import { getGame, joinGame, action, showAllCards } from "./kirvesActions"
import RenderGame from "./RenderGame"
import Icon from "../shared/Icon"
import useGameRefresh from "./useGameRefresh"

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
}) => {
  const { id } = useParams()
  useEffect(() => {
    if (id && String(game?.id) !== id) {
      getGame(id)
    }
  }, [id, game?.id])
  useGameRefresh(game?.id, refresh, !socketDisabled)
  return (
    <div>
      {game && (
        <Box sx={{ flexGrow: 1 }} className="kirves-container">
          <h1 className="game-heading">
            <Icon name="axe" size={44} />
            Kirves (Peli {game.id})
          </h1>
          <RenderGame
            game={game}
            isReplay={false}
            action={action}
            join={join}
            showAllCards={showAllCards}
            cardsVisible={cardsVisible}
          />
        </Box>
      )}
      {!user.loading && !user.email && <Navigate to="/" />}
    </div>
  )
}

const mapStateToProps = state => ({
  user: path(["user"], state),
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

export default connect(mapStateToProps, mapDispatchToProps)(KirvesGame)
