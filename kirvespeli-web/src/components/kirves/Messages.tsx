import { List, ListItem, ListItemText } from "@mui/material"

// Newest first.
const Messages = ({ messages }: { messages: readonly string[] }) =>
  messages.length ? (
    <List dense className="messages-list">
      {messages.toReversed().map((message, index) => (
        <ListItem key={`li-${index}`}>
          <ListItemText primary={message} />
        </ListItem>
      ))}
    </List>
  ) : (
    <p className="messages-empty">Ei viestejä</p>
  )

export default Messages
