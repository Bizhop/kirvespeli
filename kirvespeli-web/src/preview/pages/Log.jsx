import React from "react"

import Log from "../../components/kirves/Log"
import { logId, logItems } from "../data/logs"
import { logAction } from "../logAction"

const baseData = {
  logId,
  logItems,
  getReplay: logAction("getReplay"),
  restoreGame: logAction("restoreGame"),
}

export default {
  title: "Log",
  group: "Components",
  variants: {
    logIndex0: () => <Log {...baseData} selectedLogIndex={0} />,
    logIndex2: () => <Log {...baseData} selectedLogIndex={2} />,
  },
}
