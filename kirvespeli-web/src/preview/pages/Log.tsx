import type { PreviewPage } from "../types.ts"

import Log from "../../components/kirves/Log.tsx"
import { logId, logItems } from "../data/logs.ts"
import { logAction } from "../logAction.ts"

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
} satisfies PreviewPage
