import React from "react"
import Table from "./Table"
import ScoreCard from "./ScoreCard"
import Messages from "./Messages"

// Both live games and log replays use the same seat order and table rendering.
export const RenderGame = ({ fitViewport = false, ...props }) => (
  <div className={`game-layout${fitViewport ? " fit-viewport" : ""}`}>
    <Table {...props} />
    <aside className="game-sidebar">
      <h2>Pisteet</h2>
      <ScoreCard game={props.game} />
      <div className="messages-panel">
        <Messages messages={props.game.messages ?? []} />
      </div>
    </aside>
  </div>
)
export default RenderGame
