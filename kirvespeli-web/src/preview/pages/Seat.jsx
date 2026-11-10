import React from "react"
import Seat from "../../components/kirves/Seat"
import { tableGames, speakingGame, departureWaitingGame, cutGame } from "../data/games"
const game = tableGames[6]
const seat = (player, state = game) => (
  <div className="seat-preview" style={{ position: "relative", height: 240 }}>
    <Seat player={player} game={state} />
  </div>
)
export default {
  title: "Seat",
  group: "Components",
  variants: {
    turn: () => seat(game.players[0]),
    folded: () => seat(game.players[1]),
    sidelined: () => seat(game.players[2]),
    departing: () => seat(departureWaitingGame.players[1], departureWaitingGame),
    speaking: () => seat(speakingGame.players[1], speakingGame),
    dealer: () => seat(speakingGame.players[0], speakingGame),
    extraCard: () => seat(cutGame.players[2], cutGame),
    longNickname: () => seat({ ...game.players[3], nickname: "Hyvin pitkä pelaajan lempinimi" }),
  },
}
