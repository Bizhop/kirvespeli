import React from "react"

import SuitSelector from "../../components/kirves/SuitSelector"
import { logAction } from "../logAction"

const action = logAction("selectSuit")

export default {
  title: "SuitSelector",
  group: "Components",
  variants: {
    currentSpades: () => <SuitSelector currentTrump="SPADES" action={action} gameId={1} />,
    currentHearts: () => <SuitSelector currentTrump="HEARTS" action={action} gameId={1} />,
    currentClubs: () => <SuitSelector currentTrump="CLUBS" action={action} gameId={1} />,
    currentDiamonds: () => <SuitSelector currentTrump="DIAMONDS" action={action} gameId={1} />,
  },
}
