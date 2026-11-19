import type { PreviewPage } from "../types.ts"
import { useState } from "react"
import { Alert, Button } from "@mui/material"
import UpdateUserForm from "../../components/user/UpdateUserForm.tsx"
import { user, other } from "../data/users.ts"
import { logAction } from "../logAction.ts"

const FormPreview = ({
  reject = false,
  empty = false,
  switchUser = false,
}: {
  reject?: boolean
  empty?: boolean
  switchUser?: boolean
}) => {
  const [current, setCurrent] = useState({ ...user, nickname: empty ? "" : user.nickname })
  const [error, setError] = useState<string | null>(null)
  const submit = async (input: { nickname: string }) => {
    await logAction("updateUser")(input)
    setError(null)
    await new Promise(resolve => setTimeout(resolve, 700))
    if (reject) setError("Nimimerkki on jo käytössä")
    else setCurrent(user => ({ ...user, nickname: input.nickname }))
  }
  return (
    <>
      {switchUser && (
        <Button
          onClick={() => {
            setCurrent(other)
            setError(null)
          }}
        >
          Vaihda käyttäjää
        </Button>
      )}
      <UpdateUserForm key={current.email} onSubmit={submit} user={current} />
      {error && <Alert severity="error">{error}</Alert>}
    </>
  )
}

export default {
  title: "UpdateUserForm",
  group: "Components",
  variants: {
    form: () => <FormPreview />,
    rejected: () => <FormPreview reject />,
    emptyNickname: () => <FormPreview empty />,
    userChange: () => <FormPreview switchUser />,
  },
} satisfies PreviewPage
