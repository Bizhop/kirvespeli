import React, { useState } from "react"
import { animated, useSpring } from "@react-spring/web"
import { Grid } from "@mui/material"
import CheckIcon from "@mui/icons-material/Check"

import { SvgImage } from "../shared/images"

const flip = set => {
  set(true)
  setTimeout(() => {
    set(false)
  }, 1500)
}

const Card = ({ index, scale, card, gameId, action, actionName, roundsWon, cardsVisible, lastCard }) => {
  const [flipped, set] = useState(!roundsWon.includes(index))

  const { transform, opacity } = useSpring({
    opacity: flipped ? 1 : 0,
    transform: `rotateY(${flipped ? 180 : 0}deg)`,
    config: { mass: 10, tension: 600, friction: 80 }
  })
  return (
    <Grid
      item
      md={scale}
      key={`card-${card}`}
      sx={{ position: "relative" }}
      onClick={() => !cardsVisible && (flipped ? action({ gameId, action: actionName, index: index }) : lastCard && flip(set))}
    >
      {(cardsVisible && !flipped) ? <div><SvgImage name={card} /><CheckIcon /></div> :
        <animated.div
          className="card"
          style={{ opacity: opacity.to(o => 1 - o), transform }}
        >
          <SvgImage name="back" />
        </animated.div>
      }
      <animated.div
        className="card"
        style={{
          opacity,
          transform,
          rotateY: '180deg',
        }}>
        <SvgImage name={card} />
      </animated.div>
    </Grid>
  )
}

export default Card
