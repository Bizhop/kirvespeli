import React, { useId, useRef, useState } from "react"
import { Badge, IconButton, Popover, Tooltip } from "@mui/material"
import ChatOutlinedIcon from "@mui/icons-material/ChatOutlined"

import Messages from "./Messages"

// Game messages behind an icon next to the game title. A dot marks messages that arrived
// after the viewer last looked; the messages present when the game is opened count as seen.
const MessagesButton = ({ messages = [] }) => {
  const anchor = useRef(null)
  const [open, setOpen] = useState(false)
  const [seen, setSeen] = useState(messages.length)
  const id = useId()
  // the list can also shrink (a restored game): never keep a stale count above it
  const unseen = !open && messages.length > Math.min(seen, messages.length)
  if (seen > messages.length) setSeen(messages.length)
  const close = () => {
    setOpen(false)
    setSeen(messages.length)
  }
  return (
    <>
      <Tooltip title="Viestit">
        <IconButton
          ref={anchor}
          color="inherit"
          aria-label={unseen ? "Viestit, uusia viestejä" : "Viestit"}
          aria-haspopup="dialog"
          aria-expanded={open}
          aria-controls={open ? id : undefined}
          onClick={() => {
            setOpen(true)
            setSeen(messages.length)
          }}
          className="messages-button"
        >
          <Badge color="secondary" variant="dot" invisible={!unseen}>
            <ChatOutlinedIcon />
          </Badge>
        </IconButton>
      </Tooltip>
      <Popover
        id={id}
        open={open}
        anchorEl={anchor.current}
        onClose={close}
        anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
        transformOrigin={{ vertical: "top", horizontal: "left" }}
        slotProps={{
          paper: { className: "messages-popover", role: "dialog", "aria-label": "Viestit" },
        }}
      >
        <Messages messages={messages} />
      </Popover>
    </>
  )
}

export default MessagesButton
