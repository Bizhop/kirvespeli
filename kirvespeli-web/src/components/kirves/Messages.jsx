import React from "react"
import { List, ListItem, ListItemText } from "@mui/material"

// Newest first.
const Messages = ({ messages }) =>
  messages.length ? (
    <List dense className="messages-list">
      {[...messages].reverse().map((message, index) => (
        <ListItem key={`li-${index}`}>
          <ListItemText primary={message} />
        </ListItem>
      ))}
    </List>
  ) : (
    <p className="messages-empty">Ei viestejä</p>
  )

export default Messages
