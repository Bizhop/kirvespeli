import React, { useState } from "react"
import { Button, TextField } from "@mui/material"
import SaveAltIcon from "@mui/icons-material/SaveAlt"
import type { User } from "../../types/kirves.ts"

interface Props {
  user: User
  onSubmit: (input: { nickname: string }) => Promise<void>
  submitting?: boolean
}

const UpdateUserForm = ({ onSubmit, user, submitting = false }: Props) => {
  const initial = user.nickname ?? ""
  const [draft, setDraft] = useState({ user, nickname: initial })
  const [saving, setSaving] = useState(false)
  // Reset on a confirmed save or a different user, including an empty nickname.
  if (draft.user !== user) {
    setDraft({ user, nickname: initial })
  }
  const busy = submitting || saving
  const dirty = draft.nickname !== initial
  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (busy || !dirty) return
    setSaving(true)
    try {
      await onSubmit({ nickname: draft.nickname })
      // The auth state owns success/errors. A rejected update keeps the draft dirty.
    } finally {
      setSaving(false)
    }
  }
  return (
    <form onSubmit={submit}>
      <TextField
        name="nickname"
        label="Nickname"
        type="text"
        margin="normal"
        autoFocus
        fullWidth
        value={draft.nickname}
        disabled={busy}
        onChange={event => setDraft(current => ({ ...current, nickname: event.target.value }))}
      />
      <Button
        variant="contained"
        type="submit"
        disabled={busy || !dirty}
        startIcon={<SaveAltIcon />}
      >
        Tallenna
      </Button>
    </form>
  )
}
export default UpdateUserForm
