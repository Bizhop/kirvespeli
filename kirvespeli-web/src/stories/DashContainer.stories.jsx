import React from "react"

import { DashContainer } from "../components/dash/DashContainer"
import { user } from "./data/users"

const updateUser = () => alert("Update user")

export default {
  title: "Containers/Dash",
  component: DashContainer,
}

export const loggedIn = () => <DashContainer user={user} updateUser={updateUser} />
export const error = () => (
  <DashContainer user={user} updateUser={updateUser} error={"Error text!"} />
)
