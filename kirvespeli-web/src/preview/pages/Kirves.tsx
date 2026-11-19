import type { PreviewPage } from "../types.ts"

import { KirvesContainer } from "../../components/kirves/KirvesContainer.tsx"
import { user } from "../data/users.ts"
import { games, hand } from "../data/games.ts"
import { logId, logItems } from "../data/logs.ts"
import { logAction } from "../logAction.ts"

const baseData = {
  user,
  authStatus: "authenticated" as const,
  games,
  logItems,
  logId,
  init: logAction("init"),
  deleteGame: logAction("deleteGame"),
  getLog: logAction("getLog"),
  getReplay: logAction("getReplay"),
  restoreGame: logAction("restoreGame"),
  getGames: logAction("getGames"),
}

export default {
  title: "Kirves",
  group: "Containers",
  variants: {
    initial: () => <KirvesContainer {...baseData} />,
    loading: () => <KirvesContainer {...baseData} games={[]} loading />,
    loadError: () => (
      <KirvesContainer
        {...baseData}
        games={[]}
        error="Yhteys palvelimeen epäonnistui"
        retry={logAction("retry")}
      />
    ),
    pending: () => <KirvesContainer {...baseData} pending />,
    operationError: () => <KirvesContainer {...baseData} operationError="Et voi poistaa peliä" />,
    restored: () => <KirvesContainer {...baseData} notice="Pelitilanne palautettu" />,
    withLog: () => <KirvesContainer {...baseData} logVisible selectedLogIndex={0} />,
    withLogAndReplay: () => (
      <KirvesContainer {...baseData} logVisible selectedLogIndex={2} replay={hand.replays[2]} />
    ),
  },
} satisfies PreviewPage
