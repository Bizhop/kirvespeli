import React from "react"

import { Header } from "../../components/shared/Header"
import { logAction } from "../logAction"

const logout = logAction("logout")

export default {
  title: "Header",
  group: "Containers",
  variants: {
    loggedIn: () => <Header loggedIn={true} logout={logout} />,
    loggedOut: () => <Header loggedIn={false} logout={logout} />,
  },
}
