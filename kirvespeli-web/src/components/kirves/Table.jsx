import React from "react"
import Seat from "./Seat"
import { hasPlayStarted, playersInTurn } from "./turnState.ts"
import TableCenter, { Deck } from "./TableCenter"
import Hand from "./Hand"
import ActionBar from "./ActionBar"
import CardMotion from "./CardMotion"
import ScoreSlip from "./ScoreSlip"

// Index zero is the viewer; play proceeds clockwise from screen left.
export const seatPresets = {
  1: ["bottom"],
  2: ["bottom", "top"],
  3: ["bottom", "top-left", "top-right"],
  4: ["bottom", "left", "top", "right"],
  5: ["bottom", "left", "top-left", "top-right", "right"],
  6: ["bottom", "bottom-left", "top-left", "top", "top-right", "bottom-right"],
}
const Table = ({
  game,
  action,
  join,
  isReplay,
  cardsVisible,
  showAllCards,
  motionResetKey,
  pending = false,
}) => {
  const positions = seatPresets[game.players.length] ?? seatPresets[6]
  const actions = game.myAvailableActions ?? []
  const selectingDealer = !isReplay && actions.includes("SET_DEALER")
  const dealerIndex = game.players.findIndex(p => p.email === game.dealer)
  const playing = hasPlayStarted(game)
  const inTurn = playersInTurn(game)
  return (
    <CardMotion className="table-area" game={game} enabled={!isReplay} resetKey={motionResetKey}>
      <div className={`card-table seats-${game.players.length}`} aria-label="Pelipöytä">
        {game.players.map((player, i) => (
          <Seat
            key={player.email}
            player={player}
            game={game}
            position={positions[i]}
            playing={playing}
            pending={pending}
            inTurn={inTurn.has(player.email)}
            cardsVisible={cardsVisible}
            onSelectDealer={
              selectingDealer
                ? () =>
                    action({
                      gameId: game.id,
                      action: "SET_DEALER",
                      dealer: player.email,
                    })
                : undefined
            }
          />
        ))}
        <div className={`deck-position deck-${positions[Math.max(0, dealerIndex)]}`}>
          <Deck game={game} />
        </div>
        <TableCenter game={game} selectingDealer={selectingDealer} join={join} pending={pending} />
        <ScoreSlip game={game} animate={!isReplay} resetKey={motionResetKey} />
      </div>
      {!isReplay && (
        <div className="hand-area" aria-busy={pending}>
          <Hand game={game} action={pending ? undefined : action} />
          <ActionBar game={game} action={action} showAllCards={showAllCards} pending={pending} />
        </div>
      )}
    </CardMotion>
  )
}
export default Table
