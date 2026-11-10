import React from "react"
import RenderGame from "../../components/kirves/RenderGame"
import { dealerGame } from "../data/games"
import { logAction } from "../logAction"

const action = logAction("setDealer")
export default {
  title: "DealerSelector",
  group: "Containers",
  variants: {
    loserDeals: () => <RenderGame game={dealerGame} action={action} />,
    otherDealer: () => (
      <RenderGame game={{ ...dealerGame, dealer: dealerGame.players[2].email }} action={action} />
    ),
  },
}
