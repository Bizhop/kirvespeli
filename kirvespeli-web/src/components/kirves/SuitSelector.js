import React from "react"
import { without } from "ramda"
import { Box, Grid } from "@mui/material"

import { SvgImage } from "../shared/images"

const SuitSelector = ({ currentTrump, action, gameId }) => {
  const suits = without([currentTrump], ["CLUBS", "SPADES", "HEARTS", "DIAMONDS"])
  return (
    <Box sx={{ flexGrow: 1 }}>
      <h3>Valitse valtti</h3>
      <Grid container spacing={1}>
        {suits.map(suit => (
          <Grid item md={4} key={`suit-${suit}`}>
            <SvgImage
              name={`Suit${suit}`}
              onClick={() => action({ gameId, action: "SPEAK_SUIT", suit })}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}

export default SuitSelector
