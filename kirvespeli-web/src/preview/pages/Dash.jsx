import React from "react"

import { DashContainer } from "../../components/dash/DashContainer"
import { user } from "../data/users"
import { logAction } from "../logAction"

const handlers = {
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
        updateError="Nickname on jo käytössä"
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
}
