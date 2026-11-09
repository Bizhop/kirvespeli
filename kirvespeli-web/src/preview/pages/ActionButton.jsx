import React from "react"

import ActionButton from "../../components/kirves/ActionButton"
import { logAction } from "../logAction"

const action = logAction("action")

export default {
  title: "ActionButton",
  group: "Components",
  variants: {
    deal: () => <ActionButton label="Jaa" action={action} actionName="DEAL" gameId={1} />,
    fold: () => <ActionButton label="Mene pakkaan" action={action} actionName="FOLD" gameId={1} />,
    cut: () => <ActionButton label="Nosta" action={action} actionName="CUT" gameId={1} />,
    declineCut: () => (
      <ActionButton label="Älä nosta" action={action} actionName="CUT" gameId={1} />
    ),
    aceOrTwoKeep: () => (
      <ActionButton
        label="Pidä"
        action={action}
        actionName="ACE_OR_TWO_DECISION"
        gameId={1}
        keepExtraCard="true"
      />
    ),
    aceOrTwoDiscard: () => (
      <ActionButton
        label="Hylkää"
        action={action}
        actionName="ACE_OR_TWO_DECISION"
        gameId={1}
        keepExtraCard="false"
      />
    ),
    keep: () => (
      <ActionButton label="Päältä" action={action} actionName="SPEAK" gameId={1} speak="KEEP" />
    ),
    change: () => (
      <ActionButton
        label="Värjäisin"
        action={action}
        actionName="SPEAK"
        gameId={1}
        speak="CHANGE"
      />
    ),
    pass: () => (
      <ActionButton label="Viitenä" action={action} actionName="SPEAK" gameId={1} speak="PASS" />
    ),
  },
}
