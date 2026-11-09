import React from "react"
import { includes } from "ramda"
import { Grid, Box, Tooltip, IconButton } from "@mui/material"
import PreviewIcon from "@mui/icons-material/Preview"

import { SvgImage } from "../shared/images"
import Cards from "./Cards"
import Player from "./Player"

export const RenderGame = ({ game, isReplay, action, cardsVisible, showAllCards }) => (
  <Box sx={{ flexGrow: 1 }}>
    {game.cutCard && (
      <Grid container spacing={1}>
        <Grid item md={1}>
          Nostokortti:
        </Grid>
        <Grid item md={2}>
          <SvgImage name={game.cutCard} />
        </Grid>
        {game.secondCutCard && (
          <Grid item md={2}>
            <SvgImage name={game.secondCutCard} className="opaque50" />
          </Grid>
        )}
      </Grid>
    )}
    <Grid container spacing={1}>
      <Grid item md={1}>
        Valtti:
      </Grid>
      {game.trumpCard && (
        <Grid item md={2}>
          <SvgImage name={game.trumpCard} />
        </Grid>
      )}
      {game.trump && (
        <Grid item md={2}>
          <SvgImage name={`Suit${game.trump}`} className="opaque50" />
        </Grid>
      )}
    </Grid>
    {!isReplay && (
      <Box>
        <h4>Omat kortit:</h4>
        <Box>
          <Cards
            cards={game.myCardsInHand}
            action={action}
            gameId={game.id}
            roundsWon={[]}
            actionName={includes("DISCARD", game.myAvailableActions) ? "DISCARD" : "PLAY_CARD"}
            firstCardSuit={game.firstCardSuit}
          />
        </Box>
      </Box>
    )}
    {!isReplay && game.myExtraCard && (
      <Grid container spacing={1}>
        <Grid item md={1}>
          Ylimääräinen kortti:
        </Grid>
        <Grid item md={2}>
          <SvgImage name={game.myExtraCard} />
        </Grid>
      </Grid>
    )}
    <h2>
      Pelaajat
      {!isReplay && game.numOfPlayedRounds == 5 && (
        <Tooltip title="Näytä kortit">
          <IconButton onClick={() => showAllCards()}>
            <PreviewIcon />
          </IconButton>
        </Tooltip>
      )}
    </h2>
    <Box>
      {game.players.map((player, idx) => (
        <Player key={`player-${idx}`} player={player} game={game} cardsVisible={cardsVisible} />
      ))}
    </Box>
  </Box>
)

export default RenderGame
