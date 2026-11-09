import React from "react"
import { Grid, Box } from "@mui/material"

import Card from "./Card"
import { SvgImage } from "../shared/images"

const Cards = ({
  scale,
  cards,
  gameId,
  action,
  actionName,
  roundsWon,
  cardsVisible,
  numOfPlayedRounds,
  firstCardSuit,
}) => {
  const s = scale || 1
  return (
    <Grid container spacing={1}>
      {cards.map((card, index) => (
        <Card
          key={index}
          index={index}
          scale={s}
          card={card}
          gameId={gameId}
          action={action}
          actionName={actionName}
          roundsWon={roundsWon}
          cardsVisible={cardsVisible}
          lastCard={numOfPlayedRounds - 1 == index}
        />
      ))}
      {firstCardSuit && (
        <Grid
          size={{
            md: "grow",
          }}
        >
          <Box
            sx={{
              display: "flex",
              justifyContent: "center",
            }}
          >
            <SvgImage name={`Suit${firstCardSuit}`} className="opaque50" />
          </Box>
        </Grid>
      )}
    </Grid>
  )
}

export default Cards
