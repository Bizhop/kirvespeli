import React from "react"
import { Box, Grid } from "@mui/material"

import { SvgImage } from "../shared/images"
import translate from "../shared/translate"
import Cards from "./Cards"

const Player = ({ player, game, cardsVisible }) => (
  <Box>
    <h3>
      {player.nickname ? player.nickname : player.email}
      {game.dealer === player.email && " (J)"}
      {player.availableActions.length > 0 && " (V)"}
      {player.declaredPlayer && " (P)"}
    </h3>
    <Grid container spacing={1}>
      <Grid item md={2}>
        Toiminnot:
      </Grid>
      <Grid item md={2}>
        {player.availableActions.map(action => (
          <div key={action}>{translate(action)} </div>
        ))}
      </Grid>
    </Grid>
    <Grid container spacing={1}>
      <Grid item md={2}>
        Kortteja kädessä:
      </Grid>
      <Grid item md={1}>
        {player.cardsInHand}
      </Grid>
    </Grid>
    {player.speak && (
      <Grid container spacing={1}>
        <Grid item md={2}>
          Puhe:
        </Grid>
        <Grid item md={1}>
          {translate(player.speak)}
        </Grid>
      </Grid>
    )}
    {player.extraCard && (
      <Grid container spacing={1}>
        <Grid item md={2}>
          Ylimääräinen kortti:
        </Grid>
        <Grid item md={1}>
          <SvgImage name={player.extraCard} />
        </Grid>
      </Grid>
    )}
    <Box>
      <Cards
        cards={player.playedCards}
        roundsWon={player.roundsWon}
        action={() => {}}
        numOfPlayedRounds={game.numOfPlayedRounds}
        cardsVisible={cardsVisible}
        scale={2}
      />
    </Box>
  </Box>
)

export default Player
