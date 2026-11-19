import type { GameAction } from "../../types/kirves.ts"
import type { GameActionHandler } from "./props.ts"
import type { IconName } from "../shared/Icon.tsx"
import { Button } from "@mui/material"
import Icon from "../shared/Icon.tsx"
import SelectionButton from "./SelectionButton.tsx"

export type ButtonAction = Exclude<
  GameAction,
  { action: "PLAY_CARD" | "DISCARD" | "SPEAK_SUIT" | "SET_DEALER" }
>
export type ActionButtonProps = { command: ButtonAction } & {
  gameId: number | null
  action: GameActionHandler
  label: string
  disabled?: boolean
  icon?: IconName
}
const ActionButton = (props: ActionButtonProps) => {
  const { gameId, command } = props
  const speech = command.action === "SPEAK"
  const icon = props.icon ?? (speech ? command.speak : undefined)
  const onClick = () => {
    if (gameId == null) return
    props.action({ ...command, gameId })
  }
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
