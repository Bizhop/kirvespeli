import React from "react"

import SuitSelector from "../components/kirves/SuitSelector"

const action = params => alert("Selected: " + JSON.stringify(params))

export default {
  title: "Components/SuitSelector",
  componen: SuitSelector,
}

export const currentSpades = () => (
  <SuitSelector currentTrump={"SPADES"} action={action} gameId={1} />
)
export const currentHearts = () => (
  <SuitSelector currentTrump={"HEARTS"} action={action} gameId={1} />
)
export const currentClubs = () => <SuitSelector currentTrump={"CLUBS"} action={action} gameId={1} />
export const currentDiamonds = () => (
  <SuitSelector currentTrump={"DIAMONDS"} action={action} gameId={1} />
)
