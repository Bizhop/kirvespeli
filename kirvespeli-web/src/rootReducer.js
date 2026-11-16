import { combineReducers } from "@reduxjs/toolkit"

import kirvesReducer from "./components/kirves/kirvesReducer"

const rootReducer = combineReducers({
  kirves: kirvesReducer,
})

export default rootReducer
