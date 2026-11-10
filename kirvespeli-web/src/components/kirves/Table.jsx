import React from "react"
import Seat from "./Seat"
import { hasPlayStarted, playersInTurn } from "./turnState"
import TableCenter, { Deck } from "./TableCenter"
import Hand from "./Hand"
import ActionBar from "./ActionBar"

// Index zero is the viewer; play proceeds clockwise from screen left.
export const seatPresets = {
  1: ["bottom"],
  2: ["bottom", "top"],
  3: ["bottom", "top-left", "top-right"],
  4: ["bottom", "left", "top", "right"],
  5: ["bottom", "left", "top-left", "top-right", "right"],
  6: ["bottom", "bottom-left", "top-left", "top", "top-right", "bottom-right"],
}
const Table = ({ game, action, join, isReplay, cardsVisible, showAllCards }) => {
  const positions = seatPresets[game.players.length] ?? seatPresets[6]
  const actions = game.myAvailableActions ?? []
  const selectingDealer = !isReplay && actions.includes("SET_DEALER")
  const dealerIndex = game.players.findIndex(p => p.email === game.dealer)
  const playing = hasPlayStarted(game)
  const inTurn = playersInTurn(game)
  return (
    <div className="table-area">
      <div className={`card-table seats-${game.players.length}`} aria-label="Pelipöytä">
        {game.players.map((player, i) => (
          <Seat
            key={player.email}
            player={player}
            game={game}
            position={positions[i]}
            playing={playing}
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
        <TableCenter game={game} selectingDealer={selectingDealer} />
      </div>
      {!isReplay && (
        <div className="hand-area">
          <Hand game={game} action={action} />
          <ActionBar game={game} action={action} join={join} showAllCards={showAllCards} />
        </div>
      )}
    </div>
  )
}
export default Table
