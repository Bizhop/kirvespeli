import React from "react"
import { Tooltip } from "@mui/material"
import Icon from "../shared/Icon"
import { SvgImage } from "../shared/images"
import Cards from "./Cards"
import { hasPlayStarted, playersInTurn } from "./turnState"

export const PlayedCards = ({ player, game, cardsVisible }) => (
  <div className="played-cards">
    <Cards
      cards={player.playedCards}
      roundsWon={player.roundsWon}
      numOfPlayedRounds={game.numOfPlayedRounds}
      cardsVisible={cardsVisible}
    />
  </div>
)

const Seat = ({
  player,
  game,
  cardsVisible,
  position = "bottom",
  onSelectDealer,
  playing = hasPlayStarted(game),
  inTurn = playersInTurn(game).has(player.email),
}) => {
  const selectable = Boolean(onSelectDealer)
  const currentDealer = game.dealer === player.email
  const SeatInfo = selectable ? "button" : "div"
  const nickname = player.nickname || player.email
  const state = player.folded
    ? "Pakassa"
    : player.departurePending
      ? "Poistumassa"
      : player.inGame === false
        ? "Sivussa"
        : ""
  // a departing player's cards stay on the table until acknowledged, so the seat is not dimmed
  const inactive = state && !player.departurePending
  return (
    <div
      className={`seat seat-${position}${inactive ? " seat-inactive" : ""}${inTurn ? " seat-turn" : ""}${selectable ? " seat-selectable" : ""}`}
      data-email={player.email}
      role="group"
      aria-label={`${nickname}${inTurn ? ", vuorossa" : ""}`}
    >
      <Tooltip title={state}>
        <SeatInfo
          className="seat-info"
          type={selectable ? "button" : undefined}
          onClick={selectable && !currentDealer ? onSelectDealer : undefined}
          aria-label={
            selectable
              ? currentDealer
                ? `${nickname}, nykyinen jakaja`
                : `Valitse ${nickname} jakajaksi`
              : undefined
          }
          aria-pressed={selectable ? currentDealer : undefined}
          aria-disabled={selectable && currentDealer ? true : undefined}
        >
          <span className="seat-name">{nickname}</span>
          <span className="seat-status">
            {!playing && currentDealer && <Icon name="dealer" />}
            {player.declaredPlayer && <Icon name="declared" />}
            {!playing && player.speak && <Icon name={player.speak} />}
          </span>
          {state && <span className="seat-state">{state}</span>}
          <span className="seat-hand" aria-label={`${player.cardsInHand ?? 0} korttia kädessä`}>
            {Array.from({ length: player.cardsInHand ?? 0 }, (_, i) => (
              <SvgImage key={i} name="back" className="seat-card" />
            ))}
            {player.extraCard && (
              <SvgImage name={player.extraCard} className="seat-card extra-card" />
            )}
          </span>
        </SeatInfo>
      </Tooltip>
      <PlayedCards player={player} game={game} cardsVisible={cardsVisible} />
    </div>
  )
}
export default Seat
