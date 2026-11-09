import {
  LOGIN_SUCCESS,
  LOGIN_FAILURE,
  LOGOUT,
  UPDATE_SUCCESS,
  UPDATE_REQUEST,
  UPDATE_FAILURE,
  GOOGLE_LOGIN_FAIL,
  AUTO_LOGIN,
  AUTO_LOGIN_SUCCESS,
  AUTO_LOGIN_FAIL,
} from "./userActions"

import { pathOr } from "ramda"

const initialState = {
  email: null,
  error: null,
  loading: false,
}

const saveToken = user => {
  localStorage.setItem("kirves-token", user.jwt)
}

const resetToken = () => {
  localStorage.removeItem("kirves-token")
}

const userReducer = (state = initialState, action) => {
  const user = pathOr({}, ["payload", "data"], action)
  switch (action.type) {
    case UPDATE_REQUEST:
      return { ...state, error: null }
    case UPDATE_FAILURE:
      return {
        ...state,
        error: pathOr(
          "Käyttäjän tallennus epäonnistui",
          ["error", "response", "data", "message"],
          action,
        ),
      }
    case AUTO_LOGIN:
      return {
        ...state,
        loading: true,
      }
    case AUTO_LOGIN_FAIL:
      resetToken()
      return {
        ...initialState,
      }
    case LOGOUT:
    case LOGIN_FAILURE:
    case GOOGLE_LOGIN_FAIL:
      resetToken()
      return {
        ...initialState,
      }
    case LOGIN_SUCCESS:
    case AUTO_LOGIN_SUCCESS:
      saveToken(user)
    case UPDATE_SUCCESS:
      return {
        ...state,
        email: user.email,
        nickname: user.nickname,
        error: null,
        loading: false,
      }
    default:
      return state
  }
}

export default userReducer
