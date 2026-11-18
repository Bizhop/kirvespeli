import React from "react"
import { Button } from "@mui/material"
import Icon from "../shared/Icon"
import SelectionButton from "./SelectionButton"

const ActionButton = props => {
  const { gameId, declineCut, keepExtraCard, speak } = props
  const extraction = { gameId, declineCut, keepExtraCard, speak }
  const speech = props.actionName === "SPEAK"
  const icon = props.icon ?? (speech ? props.speak : undefined)
  const onClick = () => props.action({ ...extraction, action: props.actionName })
  if (speech)
    return (
      <SelectionButton disabled={props.disabled} label={props.label} onClick={onClick}>
        <Icon name={icon} size={48} decorative />
      </SelectionButton>
    )
  return (
    <Button
      variant="contained"
      disabled={props.disabled}
      onClick={onClick}
      startIcon={icon ? <Icon name={icon} decorative /> : undefined}
    >
      {props.label}
    </Button>
  )
}
export default ActionButton
