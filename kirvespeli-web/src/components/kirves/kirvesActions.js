import { dissoc } from "ramda"
import { deletePayload, getPayload, postPayload, putPayload } from "../Api"

export const INIT = "kirves/INIT"
export const INIT_SUCCESS = "kirves/INIT_SUCCESS"
export const INIT_FAILURE = "kirves/INIT_FAIL"
export const GET_GAME = "kirves/GET_GAME"
export const GET_GAME_SUCCESS = "kirves/GET_GAME_SUCCESS"
export const GET_GAME_FAILURE = "kirves/GET_GAME_FAIL"
export const GET_GAMES = "kirves/GET_GAMES"
export const GET_GAMES_SUCCESS = "kirves/GET_GAMES_SUCCESS"
export const GET_GAMES_FAILURE = "kirves/GET_GAMES_FAIL"
export const JOIN_GAME = "kirves/JOIN_GAME"
export const JOIN_GAME_SUCCESS = "kirves/JOIN_GAME_SUCCESS"
export const JOIN_GAME_FAILURE = "kirves/JOIN_GAME_FAIL"
export const DELETE_GAME = "kirves/DELETE_GAME"
export const DELETE_GAME_SUCCESS = "kirves/DELETE_GAME_SUCCESS"
export const DELETE_GAME_FAILURE = "kirves/DELETE_GAME_FAIL"
export const ACTION = "kirves/ACTION"
export const ACTION_SUCCESS = "kirves/ACTION_SUCCESS"
export const ACTION_FAILURE = "kirves/ACTION_FAIL"
export const GET_LOG = "kirves/GET_LOG"
export const GET_LOG_SUCCESS = "kirves/GET_LOG_SUCCESS"
export const GET_LOG_FAILURE = "kirves/GET_LOG_FAIL"
export const SHOW_ALL_CARDS = "kirves/SHOW_ALL_CARDS"
export const GET_REPLAY = "kirves/GET_REPLAY"
export const GET_REPLAY_SUCCESS = "kirves/GET_REPLAY_SUCCESS"
export const GET_REPLAY_FAILURE = "kirves/GET_REPLAY_FAIL"
export const RESTORE_GAME = "kirves/RESTORE_GAME"
export const RESTORE_GAME_SUCCESS = "kirves/RESTORE_GAME_SUCCESS"
export const RESTORE_GAME_FAILURE = "kirves/RESTORE_GAME_FAIL"

export const init = () => ({
  type: INIT,
  payload: postPayload({
    url: "api/kirves",
    data: null,
  }),
})

export const getGame = gameId => ({
  type: GET_GAME,
  payload: getPayload({
    url: `api/kirves/${gameId}`,
  }),
})

export const getGames = () => ({
  type: GET_GAMES,
  payload: getPayload({
    url: "api/kirves",
  }),
})

export const deleteGame = gameId => ({
  type: DELETE_GAME,
  gameId,
  payload: deletePayload({
    url: `api/kirves/${gameId}`,
  }),
})

export const joinGame = gameId => ({
  type: JOIN_GAME,
  payload: postPayload({
    url: `api/kirves/${gameId}`,
    data: null,
  }),
})

export const action = params => ({
  type: ACTION,
  payload: putPayload({
    url: `api/kirves/${params.gameId}`,
    data: dissoc("gameId", params),
  }),
})

export const getLog = params => ({
  type: GET_LOG,
  payload: getPayload({
    url: `api/kirves/${params.gameId}/${params.handId}`,
  }),
  params,
})

export const showAllCards = () => ({
  type: SHOW_ALL_CARDS,
})

export const getReplay = params => ({
  type: GET_REPLAY,
  payload: getPayload({
    url: `api/kirves/${params.gameId}/${params.handId}/${params.index}`,
  }),
  params,
})

export const restoreGame = params => ({
  type: RESTORE_GAME,
  payload: postPayload({
    url: `api/kirves/${params.gameId}/${params.handId}/${params.index}`,
    data: null,
  }),
})
