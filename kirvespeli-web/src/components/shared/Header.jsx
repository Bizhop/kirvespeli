import React from "react"
import { path } from "ramda"
import { NavLink } from "react-router"
import { connect } from "react-redux"
import { Box, Grid, IconButton, Tooltip } from "@mui/material"
import HomeIcon from "@mui/icons-material/Home"
import GamesIcon from "@mui/icons-material/Games"
import LogoutIcon from "@mui/icons-material/Logout"

import { logout } from "../user/userActions"

const MyNavLink = ({ to, label, icon }) => (
  <Grid
    size={{
      md: 1,
    }}
    sx={{
      textAlign: "center",
    }}
  >
    <NavLink to={to}>
      <Tooltip title={label}>
        <IconButton variant="outlined" size="small">
          {icon}
        </IconButton>
      </Tooltip>
    </NavLink>
  </Grid>
)

export const Header = ({ loggedIn, logout }) => (
  <Box sx={{ flexGrow: 1 }}>
    <Grid container spacing={1} className="site-header-row">
      <MyNavLink to="/" label="Etusivu" icon={<HomeIcon />} />
      {loggedIn && <MyNavLink to="/kirves" label="Pelit" icon={<GamesIcon />} />}
      {loggedIn && (
        <Grid
          size={{
            md: "grow",
          }}
        >
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
            }}
          >
            <Tooltip title="Kirjaudu ulos">
              <IconButton onClick={() => logout()} variant="contained" color="error">
                <LogoutIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Grid>
      )}
    </Grid>
  </Box>
)

const mapStateToProps = state => ({
  loggedIn: path(["user", "email"], state),
})

const mapDispatchToProps = dispatch => ({
  logout: () => dispatch(logout()),
})

export default connect(mapStateToProps, mapDispatchToProps)(Header)
