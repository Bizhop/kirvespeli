import type { PreviewPage } from "../types.ts"
import Messages from "../../components/kirves/Messages.tsx"
import { dealerGame } from "../data/games.ts"

// messages exactly as the backend writes them
const twoHands = ["Terhi meni pakkaan", "Voittaja: Ville"]
const gameEnd = dealerGame.messages
const longEvening = [
  "Uusi nosto, Vesa voi kieltäytyä nostamasta",
  "Vesa kieltäytyi nostosta",
  "Kalle meni pakkaan",
  "Voittajat: Ville,Terhi",
  "Poistuminen kuitattiin automaattisesti",
  "Voittaja: Vesa",
  "Hävisi: Kalle",
  "Ville valitsi jakajaksi pelaajan Terhi",
]

export default {
  title: "Messages",
  group: "Components",
  variants: {
    two: () => <Messages messages={twoHands} />,
    gameEnd: () => <Messages messages={gameEnd} />,
    many: () => <Messages messages={longEvening} />,
  },
} satisfies PreviewPage
