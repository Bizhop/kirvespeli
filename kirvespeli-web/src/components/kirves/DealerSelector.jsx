import React from "react"
import { Box, Button, Stack } from "@mui/material"

// The loser of a game deals the next one by default, but may give the deal to another player
// until the first cut.
const DealerSelector = ({ players, dealer, action, gameId }) => (
  <Box>
    <h3>Valitse jakaja</h3>
    <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: "wrap" }}>
      {players.map(player => (
        <Button
          key={player.email}
          variant="contained"
          disabled={player.email === dealer}
          onClick={() => action({ gameId, action: "SET_DEALER", dealer: player.email })}
        >
          {player.email === dealer ? `${player.nickname} (jakaja)` : player.nickname}
        </Button>
      ))}
    </Stack>
  </Box>
)

export default DealerSelector
