import React from "react"
import { GoogleLogin } from "@react-oauth/google"
import { Alert, Box, Grid, LinearProgress } from "@mui/material"

import { useAuth } from "../../auth/useAuth.ts"
import UpdateUserForm from "../user/UpdateUserForm"

export const DashContainer = ({
  user,
  status,
  loginError,
  updateError,
  loggingIn,
  updating,
  login,
  loginFailed,
  updateUser,
  // replaceable so the preview can render without GoogleOAuthProvider
  loginButton,
}) => (
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
              user={user}
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
          onSuccess={response => login(response.credential)}
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
