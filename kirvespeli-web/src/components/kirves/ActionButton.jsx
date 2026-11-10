import React from "react"
import { pick } from "ramda"
import { Button } from "@mui/material"
import Icon from "../shared/Icon"
import SelectionButton from "./SelectionButton"

const ActionButton = props => {
  const extraction = pick(["gameId", "declineCut", "keepExtraCard", "speak"])(props)
  const speech = props.actionName === "SPEAK"
  const icon = props.icon ?? (speech ? props.speak : undefined)
  const onClick = () => props.action({ ...extraction, action: props.actionName })
  if (speech)
    return (
      <SelectionButton label={props.label} onClick={onClick}>
        <Icon name={icon} size={48} decorative />
      </SelectionButton>
    )
  return (
    <Button
      variant="contained"
      onClick={onClick}
      startIcon={icon ? <Icon name={icon} decorative /> : undefined}
    >
      {props.label}
    </Button>
  )
}
export default ActionButton
