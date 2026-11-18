import React from "react"
import { Alert, Button } from "@mui/material"

const RequestStatus = ({ loading, pending, error, operationError, notice, retry }) => (
  <>
    {loading && <p role="status">Haetaan tilannetta…</p>}
    {pending && <p role="status">Tallennetaan…</p>}
    {operationError && <Alert severity="error">{operationError}</Alert>}
    {error && (
      <Alert severity="error" action={retry && <Button onClick={retry}>Yritä uudelleen</Button>}>
        {error}
      </Alert>
    )}
    {notice && <Alert severity="success">{notice}</Alert>}
  </>
)
export default RequestStatus
