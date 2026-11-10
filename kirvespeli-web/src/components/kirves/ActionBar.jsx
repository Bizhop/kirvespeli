import React from "react"
import { Button } from "@mui/material"
import ActionButton from "./ActionButton"
import SuitSelector from "./SuitSelector"
import { departingPlayers } from "./turnState"

const ActionBar = ({ game, action, join, showAllCards }) => {
  const actions = game.myAvailableActions ?? []
  const departing = departingPlayers(game)
  const button = (name, label, extra = {}) =>
    actions.includes(name) && (
      <ActionButton
        key={`${name}:${label}`}
        action={action}
        actionName={name}
        gameId={game.id}
        label={label}
        {...extra}
      />
    )
  return (
    <div className="action-bar">
      {game.canJoin && (
        <Button variant="contained" onClick={() => join?.(game.id)}>
          Liity peliin
        </Button>
      )}
      {button("DEAL", "Jaa", { icon: "dealer" })}
      {button("CUT", "Nosta")}
      {game.canDeclineCut && button("CUT", "Älä nosta", { declineCut: "true" })}
      {button("FOLD", "Mene pakkaan")}
      {button("ACE_OR_TWO_DECISION", "Pidä", { keepExtraCard: "true" })}
      {button("ACE_OR_TWO_DECISION", "Hylkää", { keepExtraCard: "false" })}
      {actions.includes("SPEAK") && (
        <div className="selection-group" role="group" aria-label="Puhe">
          <h3 className="selection-label">Puhe</h3>
          <div className="selection-options">
            {button("SPEAK", "Päältä", { speak: "KEEP", icon: "KEEP" })}
            {button("SPEAK", "Värjäisin", { speak: "CHANGE", icon: "CHANGE" })}
            {button("SPEAK", "Viitenä", { speak: "PASS", icon: "PASS" })}
          </div>
        </div>
      )}
      {actions.includes("SPEAK_SUIT") && (
        <SuitSelector currentTrump={game.trump} action={action} gameId={game.id} />
      )}
      {actions.includes("PLAY_CARD") && <span>Valitse pelattava kortti</span>}
      {actions.includes("DISCARD") && <span>Valitse hylättävä kortti</span>}
      {button("ACKNOWLEDGE_DEPARTURE", "Kuittaa poistuminen")}
      {departing.length > 0 && !actions.includes("ACKNOWLEDGE_DEPARTURE") && (
        <span>
          Odotetaan poistumisen kuittausta: {departing.map(p => p.nickname || p.email).join(", ")}
        </span>
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
