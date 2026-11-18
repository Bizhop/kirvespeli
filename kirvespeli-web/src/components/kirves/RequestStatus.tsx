import { Alert, Button } from "@mui/material"

export interface RequestStatusProps {
  loading?: boolean
  pending?: boolean
  error?: string | null
  operationError?: string | null
  notice?: string | null
  retry?: () => void
}
const RequestStatus = ({
  loading,
  pending,
  error,
  operationError,
  notice,
  retry,
}: RequestStatusProps) => (
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
