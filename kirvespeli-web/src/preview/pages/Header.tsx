import type { PreviewPage } from "../types.ts"

import { Header } from "../../components/shared/Header.tsx"
import { logAction } from "../logAction.ts"

const logout = logAction("logout")

export default {
  title: "Header",
  group: "Containers",
  variants: {
    loggedIn: () => <Header loggedIn={true} playerName="Ville" logout={logout} />,
    loggedOut: () => <Header loggedIn={false} logout={logout} />,
  },
} satisfies PreviewPage
