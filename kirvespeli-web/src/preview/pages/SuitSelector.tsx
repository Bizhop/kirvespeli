import type { PreviewPage } from "../types.ts"

import SuitSelector from "../../components/kirves/SuitSelector.tsx"
import { logAction } from "../logAction.ts"

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
} satisfies PreviewPage
