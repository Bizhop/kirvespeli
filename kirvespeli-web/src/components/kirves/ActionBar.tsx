import type { TableProps } from "./props.ts"
import type { IconName } from "../shared/Icon.tsx"
import { useEffect, useState } from "react"
import { Button } from "@mui/material"
import ActionButton, { type ButtonAction } from "./ActionButton.tsx"
import SuitSelector from "./SuitSelector.tsx"
import { departingPlayers } from "./turnState.ts"

// Counts down locally between refreshes; a new value from the server resynchronises it.
const DepartureCountdown = ({ seconds }: { seconds: number }) => {
  const [left, setLeft] = useState(seconds)
  useEffect(() => {
    setLeft(seconds)
    const timer = setInterval(() => setLeft(s => Math.max(0, s - 1)), 1000)
    return () => clearInterval(timer)
  }, [seconds])
  return <span>Peli jatkuu automaattisesti {left} s kuluttua</span>
}

const ActionBar = ({
  game,
  action,
  showAllCards,
  pending,
}: Pick<TableProps, "game" | "action" | "showAllCards" | "pending">) => {
  const actions = game.myAvailableActions ?? []
  const departing = departingPlayers(game)
  const button = (command: ButtonAction, label: string, icon?: IconName) =>
    actions.includes(command.action) && (
      <ActionButton
        key={`${command.action}:${label}`}
        action={action}
        disabled={pending}
        command={command}
        icon={icon}
        gameId={game.id}
        label={label}
      />
    )
  return (
    <div className="action-bar">
      {button({ action: "DEAL" }, "Jaa", "dealer")}
      {button({ action: "CUT" }, "Nosta")}
      {game.canDeclineCut && button({ action: "CUT", declineCut: true }, "Älä nosta")}
      {button({ action: "FOLD" }, "Mene pakkaan")}
      {button({ action: "ACE_OR_TWO_DECISION", keepExtraCard: true }, "Pidä")}
      {button({ action: "ACE_OR_TWO_DECISION", keepExtraCard: false }, "Hylkää")}
      {actions.includes("SPEAK") && (
        <div className="selection-group" role="group" aria-label="Puhe">
          <h3 className="selection-label">Puhe</h3>
          <div className="selection-options">
            {button({ action: "SPEAK", speak: "KEEP" }, "Päältä", "KEEP")}
            {button({ action: "SPEAK", speak: "CHANGE" }, "Värjäisin", "CHANGE")}
            {button({ action: "SPEAK", speak: "PASS" }, "Viitenä", "PASS")}
          </div>
        </div>
      )}
      {actions.includes("SPEAK_SUIT") && (
        <SuitSelector
          disabled={pending}
          currentTrump={game.trump}
          action={action}
          gameId={game.id}
        />
      )}
      {actions.includes("PLAY_CARD") && <span>Valitse pelattava kortti</span>}
      {actions.includes("DISCARD") && <span>Valitse hylättävä kortti</span>}
      {button({ action: "ACKNOWLEDGE_DEPARTURE" }, "Kuittaa poistuminen")}
      {button({ action: "CONTINUE_GAME" }, "Jatka peliä")}
      {departing.length > 0 && !actions.includes("ACKNOWLEDGE_DEPARTURE") && (
        <span>
          Odotetaan poistumisen kuittausta: {departing.map(p => p.nickname || p.email).join(", ")}
        </span>
      )}
      {departing.length > 0 && game.departureSecondsLeft != null && (
        <DepartureCountdown seconds={game.departureSecondsLeft} />
      )}
      {(game.numOfPlayedRounds === 5 || departing.length > 0) && (
        <Button variant="outlined" onClick={() => showAllCards?.()}>
          Näytä kortit
        </Button>
      )}
    </div>
  )
}
export default ActionBar
