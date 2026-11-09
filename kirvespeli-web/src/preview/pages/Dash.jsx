import React from "react"

import { DashContainer } from "../../components/dash/DashContainer"
import { user } from "../data/users"
import { logAction } from "../logAction"

const updateUser = logAction("updateUser")

export default {
  title: "Dash",
  group: "Containers",
  variants: {
    loggedIn: () => <DashContainer user={user} updateUser={updateUser} />,
    error: () => <DashContainer user={user} updateUser={updateUser} error="Error text!" />,
  },
}
