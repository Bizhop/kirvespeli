import React from "react"
import Seat from "./Seat"

// Standalone player view for existing consumers; the table places Seat directly.
const Player = props => (
  <div className="player-preview">
    <Seat {...props} />
  </div>
)
export default Player
