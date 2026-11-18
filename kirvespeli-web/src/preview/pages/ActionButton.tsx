import type { PreviewPage } from "../types.ts"

import ActionButton from "../../components/kirves/ActionButton.tsx"
import { logAction } from "../logAction.ts"

const action = logAction("action")

export default {
  title: "ActionButton",
  group: "Components",
  variants: {
    deal: () => (
      <ActionButton label="Jaa" action={action} command={{ action: "DEAL" }} gameId={1} />
    ),
    fold: () => (
      <ActionButton label="Mene pakkaan" action={action} command={{ action: "FOLD" }} gameId={1} />
    ),
    cut: () => (
      <ActionButton label="Nosta" action={action} command={{ action: "CUT" }} gameId={1} />
    ),
    declineCut: () => (
      <ActionButton
        label="Älä nosta"
        action={action}
        command={{ action: "CUT", declineCut: true }}
        gameId={1}
      />
    ),
    aceOrTwoKeep: () => (
      <ActionButton
        label="Pidä"
        action={action}
        command={{ action: "ACE_OR_TWO_DECISION", keepExtraCard: true }}
        gameId={1}
      />
    ),
    aceOrTwoDiscard: () => (
      <ActionButton
        label="Hylkää"
        action={action}
        command={{ action: "ACE_OR_TWO_DECISION", keepExtraCard: false }}
        gameId={1}
      />
    ),
    keep: () => (
      <ActionButton
        label="Päältä"
        action={action}
        command={{ action: "SPEAK", speak: "KEEP" }}
        gameId={1}
      />
    ),
    change: () => (
      <ActionButton
        label="Värjäisin"
        action={action}
        command={{ action: "SPEAK", speak: "CHANGE" }}
        gameId={1}
      />
    ),
    pass: () => (
      <ActionButton
        label="Viitenä"
        action={action}
        command={{ action: "SPEAK", speak: "PASS" }}
        gameId={1}
      />
    ),
  },
} satisfies PreviewPage
