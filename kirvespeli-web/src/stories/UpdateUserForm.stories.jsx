import React from "react"

import UpdateUserForm from "../components/user/UpdateUserForm"
import { user } from "./data/users"

const updateUser = data => alert("Update user, data: " + JSON.stringify(data))

export default {
  title: "Components/UpdateUserForm",
  component: UpdateUserForm,
}

export const form = () => <UpdateUserForm onSubmit={updateUser} user={user} />
