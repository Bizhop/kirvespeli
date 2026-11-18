import type { GameActionRequest, GameOut } from "../../types/kirves.ts"

export type GameActionHandler = (request: GameActionRequest) => void
export interface TableProps {
  game: GameOut
  action: GameActionHandler
  join?: (id: number) => void
  isReplay?: boolean
  cardsVisible?: boolean
  showAllCards?: () => void
  motionResetKey?: number
  pending?: boolean
}
