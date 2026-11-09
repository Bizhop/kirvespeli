import {
  LOGIN_SUCCESS,
  LOGIN_FAILURE,
  LOGOUT,
  UPDATE_SUCCESS,
  GOOGLE_LOGIN_FAIL,
  AUTO_LOGIN_SUCCESS,
} from "./userActions"

import { pathOr } from "ramda"

const initialState = {
  email: null,
  error: null,
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
      }
    default:
      return state
  }
}

export default userReducer
