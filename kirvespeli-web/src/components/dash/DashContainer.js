import React from "react"
import { path } from "ramda"
import { connect } from "react-redux"
import { GoogleLogin } from "@react-oauth/google"
import { Alert, Box, Grid } from "@mui/material"

import { login, update, googleLoginError } from "../user/userActions"
import UpdateUserForm from "../user/UpdateUserForm"

export const DashContainer = ({ user, error, updateUser, login, loginError }) => (
  <div>
    {user.email ? (
      <Box sx={{ flexGrow: 1 }}>
        <h1>Tervetuloa!</h1>
        <Grid container spacing={1}>
          <Grid item md={2}>
            <strong>Email</strong>
          </Grid>
          <Grid item md={5}>
            {user.email}
          </Grid>
        </Grid>
        <Grid container spacing={1}>
          <Grid item md={2}>
            <strong>Nickname</strong>
          </Grid>
          <Grid item md={5}>
            {user.nickname}
          </Grid>
        </Grid>
        <Grid container spacing={1}>
          <Grid item md={4}>
            <UpdateUserForm onSubmit={updateUser} user={user} />
          </Grid>
        </Grid>
      </Box>
    ) : (
      <GoogleLogin onSuccess={login} onError={loginError} useOneTap />
    )}
    {error && <Alert severity="error">{error}</Alert>}
  </div>
)

const mapStateToProps = state => ({
  user: path(["user"], state),
  error: path(["user", "error"], state),
})

const mapDispatchToProps = dispatch => ({
  login: response => dispatch(login(response)),
  loginError: response => dispatch(googleLoginError(response)),
  updateUser: form => dispatch(update(form)),
})

export default connect(mapStateToProps, mapDispatchToProps)(DashContainer)
