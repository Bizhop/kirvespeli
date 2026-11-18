import type { AuthState, Session } from "../../auth/session.ts"
import type React from "react"
import { GoogleLogin } from "@react-oauth/google"
import { Alert, Box, Grid, LinearProgress } from "@mui/material"

import { useAuth } from "../../auth/useAuth.ts"
import UpdateUserForm from "../user/UpdateUserForm.tsx"

export const DashContainer = ({
  user,
  status,
  loginError = null,
  updateError = null,
  loggingIn = false,
  updating = false,
  login,
  loginFailed,
  updateUser,
  // replaceable so the preview can render without GoogleOAuthProvider
  loginButton,
}: Pick<AuthState, "status" | "user"> &
  Partial<Pick<AuthState, "loginError" | "updateError" | "loggingIn" | "updating">> &
  Pick<Session, "login" | "loginFailed" | "updateUser"> & { loginButton?: React.ReactNode }) => (
  <div>
    {(status === "restoring" || loggingIn || updating) && <LinearProgress />}
    {status === "authenticated" && user ? (
      <Box sx={{ flexGrow: 1 }}>
        <h1>Tervetuloa, {user.nickname || user.email}!</h1>
        <Grid container spacing={1}>
          <Grid
            size={{
              md: 2,
            }}
          >
            <strong>Email</strong>
          </Grid>
          <Grid
            size={{
              md: 5,
            }}
          >
            {user.email}
          </Grid>
        </Grid>
        <Grid container spacing={1}>
          <Grid
            size={{
              md: 2,
            }}
          >
            <strong>Nickname</strong>
          </Grid>
          <Grid
            size={{
              md: 5,
            }}
          >
            {user.nickname}
          </Grid>
        </Grid>
        <Grid container spacing={1}>
          <Grid
            size={{
              md: 4,
            }}
          >
            <UpdateUserForm
              onSubmit={values => updateUser({ nickname: values.nickname })}
              key={user.email}
              user={user}
              submitting={updating}
            />
          </Grid>
        </Grid>
      </Box>
    ) : (
      // Only when anonymous, never while restoring: unmounting cancels a pending One Tap
      // (FedCM) prompt, which GSI logs as an AbortError.
      status === "anonymous" &&
      (loginButton ?? (
        <GoogleLogin
          onSuccess={response => (response.credential ? login(response.credential) : loginFailed())}
          onError={loginFailed}
          useOneTap
        />
      ))
    )}
    {loginError && <Alert severity="error">{loginError}</Alert>}
    {updateError && <Alert severity="error">{updateError}</Alert>}
  </div>
)

const DashContainerWithAuth = () => <DashContainer {...useAuth()} />

export default DashContainerWithAuth
