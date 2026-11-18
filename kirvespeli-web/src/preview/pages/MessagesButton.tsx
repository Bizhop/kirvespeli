import type { PreviewPage } from "../types.ts"
import { useState } from "react"
import { Box, Button } from "@mui/material"

import MessagesButton from "../../components/kirves/MessagesButton.tsx"
import { dealerGame } from "../data/games.ts"

const more = ["Kalle meni pakkaan", "Voittajat: Ville,Terhi"]

// New messages after opening the game show a dot until the list is opened.
const Live = () => {
  const [count, setCount] = useState(0)
  const messages = [...dealerGame.messages, ...more.slice(0, count)]
  return (
    <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
      <h1 className="game-heading">
        Kirves (Peli 12)
        <MessagesButton messages={messages} />
      </h1>
      <Button disabled={count >= more.length} onClick={() => setCount(count + 1)}>
        Uusi viesti
      </Button>
    </Box>
  )
}

export default {
  title: "MessagesButton",
  group: "Components",
  variants: {
    live: () => <Live />,
    empty: () => (
      <h1 className="game-heading">
        Kirves (Peli 12)
        <MessagesButton messages={[]} />
      </h1>
    ),
  },
} satisfies PreviewPage
