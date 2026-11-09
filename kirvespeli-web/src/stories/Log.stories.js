import React from "react"

import Log from "../components/kirves/Log"
import { logId, logItems } from "./data/logs"

const getReplay = data => alert("getReplay, data: " + JSON.stringify(data))
const restoreGame = data => alert("restoreGame, data: " + JSON.stringify(data))

export default {
  title: "Components/Log",
  component: Log,
}

export const logIndex0 = () => (
  <Log
    logId={logId}
    logItems={logItems}
    selectedLogIndex={0}
    getReplay={getReplay}
    restoreGame={restoreGame}
  />
)
export const logIndex2 = () => (
  <Log
    logId={logId}
    logItems={logItems}
    selectedLogIndex={2}
    getReplay={getReplay}
    restoreGame={restoreGame}
  />
)
