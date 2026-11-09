import { loginPayload, putPayload } from "../Api"

export const AUTO_LOGIN = "user/AUTO_LOGIN"
export const AUTO_LOGIN_SUCCESS = "user/AUTO_LOGIN_SUCCESS"
export const AUTO_LOGIN_FAIL = "user/AUTO_LOGIN_FAIL"
export const GOOGLE_LOGIN_FAIL = "user/GOOGLE_LOGIN_FAIL"

export const LOGIN_REQUEST = "user/LOGIN"
export const LOGIN_SUCCESS = "user/LOGIN_SUCCESS"
export const LOGIN_FAILURE = "user/LOGIN_FAIL"
export const LOGOUT = "user/LOGOUT"
export const UPDATE_REQUEST = "user/UPDATE"
export const UPDATE_SUCCESS = "user/UPDATE_SUCCESS"
export const UPDATE_FAILURE = "user/UPDATE_FAIL"

export const autoLogin = () => ({
  type: AUTO_LOGIN,
  payload: loginPayload({ tokenId: localStorage.getItem("kirves-token") }),
})

export const googleLoginError = error => ({
  type: GOOGLE_LOGIN_FAIL,
  error,
})

export const login = params => ({
  type: LOGIN_REQUEST,
  payload: loginPayload({ tokenId: params.credential }),
})

export const logout = () => ({
  type: LOGOUT,
})

export const update = params => ({
  type: UPDATE_REQUEST,
  payload: putPayload({
    url: "api/user",
    data: params,
  }),
})
