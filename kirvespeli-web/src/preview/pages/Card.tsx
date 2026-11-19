import type { PreviewPage } from "../types.ts"
import type React from "react"
import { Grid } from "@mui/material"

import Card from "../../components/kirves/Card.tsx"
import { logAction } from "../logAction.ts"

const baseData: React.ComponentProps<typeof Card> = {
  index: 1,
  card: "KH",
  gameId: 1,
  action: logAction("action"),
  actionName: "PLAY_CARD",
  roundsWon: [1],
  cardsVisible: false,
  lastCard: true,
}

export default {
  title: "Card",
  group: "Components",
  variants: {
    card: () => (
      <Grid container spacing={1}>
        <Card {...baseData} />
      </Grid>
    ),
  },
} satisfies PreviewPage
