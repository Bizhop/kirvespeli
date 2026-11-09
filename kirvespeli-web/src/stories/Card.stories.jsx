import React from "react"
import { Grid } from "@mui/material"

import Card from "../components/kirves/Card"

const baseData = {
  index: 1,
  scale: 1,
  card: "KH",
  gameId: 1,
  action: data => alert(JSON.stringify(data)),
  actionName: "PLAY_CARD",
  roundsWon: [1],
  cardsVisible: false,
  lastCard: true,
}

export default {
  title: "Components/Card",
  component: Card,
}

export const card = () => (
  <Grid container spacing={1}>
    <Card {...baseData} />
  </Grid>
)
