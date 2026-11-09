import { configureStore } from "@reduxjs/toolkit"
import axios from "axios"

import rootReducer from "./rootReducer"
import { autoLogin } from "./components/user/userActions"

const baseURL = process.env.API_URL

const client = axios.create({
  baseURL: baseURL,
  responseType: "json",
})

// Actions with payload.request are sent with axios. The action itself is passed on first,
// then <type>_SUCCESS with the axios response as payload, or <type>_FAIL with the axios
// error, both with meta.previousAction (same contract as redux-axios-middleware).
const axiosMiddleware = () => next => action => {
  if (!action.payload?.request) {
    return next(action)
  }
  next(action)
  const meta = { previousAction: action }
  return client.request(action.payload.request).then(
    response => next({ type: `${action.type}_SUCCESS`, payload: response, meta }),
    error =>
      next({
        type: `${action.type}_FAIL`,
        error: error.response ? error : { data: error.message, status: 0 },
        meta,
      }),
  )
}

const Store = () => {
  const store = configureStore({
    reducer: rootReducer,
    middleware: getDefaultMiddleware =>
      getDefaultMiddleware({ serializableCheck: false }).concat(axiosMiddleware),
  })

  store.dispatch(autoLogin())

  return store
}

export default Store
