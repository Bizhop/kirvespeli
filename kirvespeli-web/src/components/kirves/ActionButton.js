import React from "react"
import { pick } from "ramda"
import { Button } from "@mui/material"

const ActionButton = props => {
  const extraction = pick(["gameId", "declineCut", "keepExtraCard", "speak"])(props)
  return (
    <Button
      variant="contained"
      onClick={() => props.action({ ...extraction, action: props.actionName })}
    >
      {props.label}
    </Button>
  )
}

export default ActionButton
