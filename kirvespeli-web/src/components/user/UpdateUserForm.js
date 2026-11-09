import React from "react"
import { Field, Form } from "react-final-form"
import { Button } from "@mui/material"
import SaveAltIcon from "@mui/icons-material/SaveAlt"

import { RenderTextInput } from "../shared/formInput"

const UpdateUserForm = ({ onSubmit, user }) => (
  <Form onSubmit={onSubmit} initialValues={user}>
    {({ handleSubmit, pristine, submitting }) => (
      <form onSubmit={handleSubmit}>
        <Field name="nickname" label="Nickname" type="text" component={RenderTextInput} />
        <Button
          variant="contained"
          type="submit"
          disabled={submitting || pristine}
          startIcon={<SaveAltIcon />}
        >
          Tallenna
        </Button>
      </form>
    )}
  </Form>
)

export default UpdateUserForm
