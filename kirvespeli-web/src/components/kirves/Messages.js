import React from "react"
import { List, ListItem, ListItemText, Box, Grid } from "@mui/material"
import { reverse } from "ramda"

const Messages = ({ messages }) => (
  <Box>
    <h4 style={{ textAlign: "right" }}>Viestit:</h4>
    <List dense>
      {reverse(messages).map((message, index) => (
        <ListItem key={`li-${index}`}>
          <ListItemText primary={message} />
        </ListItem>
      ))}
    </List>
  </Box>
)

export default Messages
