import type { PreviewPage } from "../types.ts"

import { DashContainer } from "../../components/dash/DashContainer.tsx"
import { user } from "../data/users.ts"
import { logAction } from "../logAction.ts"

const loginButton = (
  <button onClick={() => logAction("login")("credential")}>Google-kirjautuminen</button>
)

const handlers = {
  loginButton,
  login: logAction("login"),
  loginFailed: logAction("loginFailed"),
  updateUser: logAction("updateUser"),
}

export default {
  title: "Dash",
  group: "Containers",
  variants: {
    loggedIn: () => <DashContainer {...handlers} status="authenticated" user={user} />,
    updating: () => <DashContainer {...handlers} status="authenticated" user={user} updating />,
    updateError: () => (
      <DashContainer
        {...handlers}
        status="authenticated"
        user={user}
        updateError="Nimimerkki on jo käytössä"
      />
    ),
    loggedOut: () => <DashContainer {...handlers} status="anonymous" user={null} />,
    loginError: () => (
      <DashContainer
        {...handlers}
        status="anonymous"
        user={null}
        loginError="Kirjautuminen epäonnistui"
      />
    ),
    restoring: () => <DashContainer {...handlers} status="restoring" user={null} />,
  },
} satisfies PreviewPage
