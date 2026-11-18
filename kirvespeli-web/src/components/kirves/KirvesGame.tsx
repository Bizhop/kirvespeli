import type { AuthStatus } from "../../auth/session.ts"
import type { GameOut } from "../../types/kirves.ts"
import type { TableProps } from "./props.ts"
import { Navigate, useParams } from "react-router"
import { Box } from "@mui/material"

import { useGame } from "../../game/hooks.ts"
import RequestStatus, { type RequestStatusProps } from "./RequestStatus.tsx"
import RenderGame from "./RenderGame.tsx"
import Icon from "../shared/Icon.tsx"
import MessagesButton from "./MessagesButton.tsx"
import useGameRefresh from "./useGameRefresh.ts"

export const KirvesGame = ({
  authStatus,
  game,
  cardsVisible,
  join,
  action,
  showAllCards,
  resetKey = 0,
  ...status
}: Pick<TableProps, "action" | "showAllCards" | "join" | "cardsVisible"> & {
  game?: GameOut | null
  authStatus?: AuthStatus
  resetKey?: number
} & RequestStatusProps) => {
  return (
    <div>
      <RequestStatus {...status} loading={!game && status.loading} pending={false} />
      {game && (
        <Box sx={{ flexGrow: 1 }} className="kirves-container">
          <h1 className="game-heading">
            <Icon name="axe" size={44} />
            Kirves (Peli {game.id})
            <MessagesButton key={game.id} messages={game.messages ?? []} />
          </h1>
          <RenderGame
            game={game}
            isReplay={false}
            fitViewport
            action={action}
            join={join}
            showAllCards={showAllCards}
            cardsVisible={cardsVisible}
            motionResetKey={resetKey}
            pending={status.pending}
          />
        </Box>
      )}
      {authStatus === "anonymous" && <Navigate to="/" />}
    </div>
  )
}

const KirvesGameWithState = () => {
  const { id } = useParams()
  const state = useGame(Number(id))
  const validId = Number.isSafeInteger(Number(id)) && Number(id) >= 0
  useGameRefresh(Number(id), state.refresh, state.enabled && validId, state.resync)
  if (state.authStatus === "restoring") return <p role="status">Palautetaan istuntoa…</p>
  if (state.authStatus === "anonymous") return <Navigate to="/" />
  if (!validId) return <p role="alert">Virheellinen pelinumero</p>
  return <KirvesGame {...state} retry={state.refresh} />
}

export default KirvesGameWithState
