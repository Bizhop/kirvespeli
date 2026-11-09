import { legacy_createStore as createStore, applyMiddleware, compose } from "redux"
import { composeWithDevTools } from "redux-devtools-extension"
import axios from "axios"
import axiosMiddleware from "redux-axios-middleware"

import rootReducer from "./rootReducer"
import { autoLogin } from "./components/user/userActions"

const env = process.env.NODE_ENV
const baseURL = process.env.API_URL

const client = axios.create({
  baseURL: baseURL,
  responseType: "json",
})

const axiosMW = axiosMiddleware(client)

const Store = () => {
  const middlewares = [axiosMW]

  const enhancer =
    env === "production"
      ? compose(applyMiddleware(...middlewares))
      : composeWithDevTools(applyMiddleware(...middlewares))

  const store = createStore(rootReducer, enhancer)

  store.dispatch(autoLogin())

  return store
}

export default Store
