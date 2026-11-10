import React from "react"
import { SvgImage } from "../shared/images"
import translate from "../shared/translate"
import SelectionButton from "./SelectionButton"

const SuitSelector = ({ currentTrump, action, gameId }) => (
  <div className="selection-group" role="group" aria-label="Valitse valtti">
    <h3 className="selection-label">Valitse valtti</h3>
    <div className="selection-options suit-options">
      {["CLUBS", "SPADES", "HEARTS", "DIAMONDS"]
        .filter(suit => suit !== currentTrump)
        .map(suit => (
          <SelectionButton
            key={suit}
            label={translate(suit)}
            onClick={() => action({ gameId, action: "SPEAK_SUIT", suit })}
          >
            <SvgImage
              name={`Suit${suit}`}
              className="status-icon"
              width={48}
              height={48}
              alt=""
              aria-hidden="true"
            />
          </SelectionButton>
        ))}
    </div>
  </div>
)
export default SuitSelector
