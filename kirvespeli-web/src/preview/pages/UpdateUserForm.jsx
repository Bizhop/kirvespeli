import React from "react"

import UpdateUserForm from "../../components/user/UpdateUserForm"
import { user } from "../data/users"
import { logAction } from "../logAction"

export default {
  title: "UpdateUserForm",
  group: "Components",
  variants: {
    form: () => <UpdateUserForm onSubmit={logAction("updateUser")} user={user} />,
  },
}
