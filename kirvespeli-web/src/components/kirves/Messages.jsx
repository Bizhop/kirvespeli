import React from "react"
import { List, ListItem, ListItemText } from "@mui/material"
import { reverse } from "ramda"

// Newest first.
const Messages = ({ messages }) =>
  messages.length ? (
    <List dense className="messages-list">
      {reverse(messages).map((message, index) => (
        <ListItem key={`li-${index}`}>
          <ListItemText primary={message} />
        </ListItem>
      ))}
    </List>
  ) : (
    <p className="messages-empty">Ei viestejä</p>
  )

export default Messages
