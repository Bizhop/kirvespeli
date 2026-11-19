import type { SuitName } from "../../types/kirves.ts"
import type { GameActionHandler } from "./props.ts"
import { SvgImage } from "../shared/images.tsx"
import translate from "../shared/translate.ts"
import SelectionButton from "./SelectionButton.tsx"

const SuitSelector = ({
  currentTrump,
  action,
  gameId,
  disabled,
}: {
  currentTrump?: SuitName | null
  action: GameActionHandler
  gameId: number | null
  disabled?: boolean
}) => (
  <div className="selection-group" role="group" aria-label="Valitse valtti">
    <h3 className="selection-label">Valitse valtti</h3>
    <div className="selection-options suit-options">
      {(["CLUBS", "SPADES", "HEARTS", "DIAMONDS"] as const)
        .filter(suit => suit !== currentTrump)
        .map(suit => (
          <SelectionButton
            key={suit}
            disabled={disabled}
            label={translate(suit)}
            onClick={() => gameId != null && action({ gameId, action: "SPEAK_SUIT", suit })}
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
