import { useId, useRef, useState, type ToggleEvent } from "react"
import { Badge, IconButton, Tooltip } from "@mui/material"
import ChatOutlinedIcon from "@mui/icons-material/ChatOutlined"

import Messages from "./Messages.tsx"

const anchorPositioning = () => CSS.supports("position-area: block-end")

// Game messages behind an icon next to the game title. A dot marks messages that arrived
// after the viewer last looked; the messages present when the game is opened count as seen.
// The panel is a non-modal <dialog popover>: the browser handles light dismiss, Esc and focus.
const MessagesButton = ({ messages = [] }: { messages?: readonly string[] }) => {
  const anchor = useRef<HTMLButtonElement>(null)
  const [open, setOpen] = useState(false)
  const [seen, setSeen] = useState(messages.length)
  const id = useId()
  const anchorName = `--messages-${id.replace(/[^\w-]/g, "")}`
  // the list can also shrink (a restored game): never keep a stale count above it
  const unseen = !open && messages.length > Math.min(seen, messages.length)
  if (seen > messages.length) setSeen(messages.length)
  const onToggle = (event: ToggleEvent<HTMLDialogElement>) => {
    const opened = event.newState === "open"
    setOpen(opened)
    setSeen(messages.length)
    if (opened && !anchorPositioning()) placeUnder(event.currentTarget, anchor.current)
  }
  return (
    <>
      <Tooltip title="Viestit">
        <IconButton
          ref={anchor}
          color="inherit"
          aria-label={unseen ? "Viestit, uusia viestejä" : "Viestit"}
          {...({ commandfor: id, command: "toggle-popover" } as object)}
          className="messages-button"
          style={{ anchorName }}
        >
          <Badge color="secondary" variant="dot" invisible={!unseen}>
            <ChatOutlinedIcon />
          </Badge>
        </IconButton>
      </Tooltip>
      <dialog
        id={id}
        popover="auto"
        aria-label="Viestit"
        className="messages-popover"
        style={{ positionAnchor: anchorName }}
        onToggle={onToggle}
      >
        <Messages messages={messages} />
      </dialog>
    </>
  )
}

// Without anchor positioning (Firefox) the popover would sit centred in the viewport:
// place it under the button, kept inside the viewport.
const placeUnder = (panel: HTMLElement, button: HTMLElement | null) => {
  if (!button) return
  const margin = 8
  const at = button.getBoundingClientRect()
  const { width, height } = panel.getBoundingClientRect()
  const left = Math.max(margin, Math.min(at.left, innerWidth - width - margin))
  const below = at.bottom + 4
  const top = below + height <= innerHeight - margin ? below : Math.max(margin, at.top - 4 - height)
  Object.assign(panel.style, { inset: "auto", margin: "0", top: `${top}px`, left: `${left}px` })
}

export default MessagesButton
