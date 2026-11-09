import React from "react"

import ActionButton from "../components/kirves/ActionButton"

const action = data => alert("Action, data: " + JSON.stringify(data))

export default {
  title: "Components/ActionButton",
  component: ActionButton,
}

export const deal = () => <ActionButton label="Jaa" action={action} actionName="DEAL" gameId={1} />
export const fold = () => (
  <ActionButton label="Mene pakkaan" action={action} actionName="FOLD" gameId={1} />
)
export const cut = () => <ActionButton label="Nosta" action={action} actionName="CUT" gameId={1} />
export const declineCut = () => (
  <ActionButton label="Älä nosta" action={action} actionName="CUT" gameId={1} />
)
export const aceOrTwoKeep = () => (
  <ActionButton
    label="Pidä"
    action={action}
    actionName="ACE_OR_TWO_DECISION"
    gameId={1}
    keepExtraCard="true"
  />
)
export const aceOrTwoDiscard = () => (
  <ActionButton
    label="Hylkää"
    action={action}
    actionName="ACE_OR_TWO_DECISION"
    gameId={1}
    keepExtraCard="false"
  />
)
export const keep = () => (
  <ActionButton label="Päältä" action={action} actionName="SPEAK" gameId={1} speak="KEEP" />
)
export const change = () => (
  <ActionButton label="Värjäisin" action={action} actionName="SPEAK" gameId={1} speak="CHANGE" />
)
export const pass = () => (
  <ActionButton label="Viitenä" action={action} actionName="SPEAK" gameId={1} speak="PASS" />
)
