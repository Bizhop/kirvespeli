import { path, pathOr, pick, map, reject } from "ramda"
import { UPDATE_FAILURE } from "../user/userActions"

import {
  INIT_FAILURE,
  GET_GAME_SUCCESS,
  GET_GAMES_SUCCESS,
  GET_GAMES_FAILURE,
  JOIN_GAME_FAILURE,
  DELETE_GAME_SUCCESS,
  ACTION_FAILURE,
  GET_GAME_FAILURE,
  SHOW_ALL_CARDS,
  GET_LOG,
  GET_LOG_SUCCESS,
  GET_LOG_FAILURE,
  GET_REPLAY_SUCCESS,
  GET_REPLAY_FAILURE,
  GET_REPLAY,
  INIT_SUCCESS,
  RESTORE_GAME_SUCCESS,
  GET_GAMES,
} from "./kirvesActions"

const initialState = {
  game: null,
  games: [],
  cardsVisible: false,
  logId: "",
  logItems: [],
  logVisible: false,
  replay: null,
  selectedLogIndex: 0,
  gamesFetched: false,
}

const kirvesReducer = (state = initialState, action) => {
  switch (action.type) {
    case GET_GAME_SUCCESS:
      return {
        ...state,
        game: action.payload.data,
        cardsVisible: false,
      }
    case GET_GAMES:
      return {
        ...state,
        gamesFetched: true,
      }
    case GET_GAMES_SUCCESS:
    case INIT_SUCCESS:
      return {
        ...state,
        games: map(
          pick(["id", "canJoin", "players", "lastHandId", "createdAt", "admin"]),
          action.payload.data
        ),
        logVisible: false,
        logItems: [],
        logId: "",
        replay: null,
        selectedLogIndex: 0,
      }
    case DELETE_GAME_SUCCESS:
      return {
        ...state,
        games: reject(game => game.id === action.meta.previousAction.gameId, state.games),
      }
    case GET_LOG: {
      return {
        ...state,
        logId: action.params,
      }
    }
    case GET_LOG_SUCCESS:
      return {
        ...state,
        logItems: action.payload.data,
        logVisible: true,
      }
    case GET_LOG_FAILURE:
      alert(path(["error", "response", "data", "message"], action))
      return {
        ...state,
        logItems: [],
        logVisible: false,
        logId: "",
      }
    case GET_REPLAY:
      return {
        ...state,
        selectedLogIndex: pathOr(0, ["params", "index"], action),
      }
    case GET_REPLAY_SUCCESS:
      return {
        ...state,
        replay: action.payload.data,
      }
    case GET_REPLAY_FAILURE:
      return {
        ...state,
        replay: null,
      }
    case SHOW_ALL_CARDS:
      return {
        ...state,
        cardsVisible: true,
      }
    case RESTORE_GAME_SUCCESS:
      alert("Pelitilanne palautettu")
      return {
        ...state,
        logItems: [],
        logVisible: false,
        logId: "",
        replay: null,
        game: null,
      }
    case JOIN_GAME_FAILURE:
    case ACTION_FAILURE:
    case INIT_FAILURE:
    case GET_GAMES_FAILURE:
    case GET_GAME_FAILURE:
    case UPDATE_FAILURE:
      alert(path(["error", "response", "data", "message"], action))
      return state
    default:
      return state
  }
}

export default kirvesReducer
