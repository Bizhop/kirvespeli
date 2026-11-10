import React from "react"

import DealerSelector from "../../components/kirves/DealerSelector"
import { scoreSheetGame } from "../data/games"
import { logAction } from "../logAction"

const action = logAction("setDealer")
const players = scoreSheetGame.scores

export default {
  title: "DealerSelector",
  group: "Components",
  variants: {
    loserDeals: () => (
      <DealerSelector players={players} dealer={players[0].email} action={action} gameId={1} />
    ),
    otherDealer: () => (
      <DealerSelector players={players} dealer={players[2].email} action={action} gameId={1} />
    ),
  },
}
