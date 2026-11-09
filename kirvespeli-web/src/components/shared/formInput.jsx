import React from "react"
import { TextField } from "@mui/material"

export const RenderTextInput = ({ input, label }) => (
  <TextField margin="normal" autoFocus fullWidth label={label} {...input} type="text" />
)
