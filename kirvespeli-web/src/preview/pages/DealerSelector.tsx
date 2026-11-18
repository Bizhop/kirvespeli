import type { PreviewPage } from "../types.ts"
import RenderGame from "../../components/kirves/RenderGame.tsx"
import { dealerGame, otherDealerGame } from "../data/games.ts"
import { logAction } from "../logAction.ts"

const action = logAction("setDealer")
export default {
  title: "DealerSelector",
  group: "Containers",
  variants: {
    loserDeals: () => <RenderGame game={dealerGame} action={action} />,
    otherDealer: () => <RenderGame game={otherDealerGame} action={action} />,
  },
} satisfies PreviewPage
