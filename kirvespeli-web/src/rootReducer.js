import { combineReducers } from "redux"

import userReducer from "./components/user/userReducer"
import kirvesReducer from "./components/kirves/kirvesReducer"

const rootReducer = combineReducers({
  user: userReducer,
  kirves: kirvesReducer,
})

export default rootReducer
