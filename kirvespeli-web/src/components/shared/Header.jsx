import React from "react"
import { NavLink } from "react-router"
import { Box, Grid, IconButton, Tooltip, Typography } from "@mui/material"
import HomeIcon from "@mui/icons-material/Home"
import GamesIcon from "@mui/icons-material/Games"
import LogoutIcon from "@mui/icons-material/Logout"

import { useAuth } from "../../auth/useAuth.ts"

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

export const Header = ({ loggedIn, playerName, logout }) => (
  <Box sx={{ flexGrow: 1 }}>
    <Grid container spacing={1} className="site-header-row">
      <MyNavLink to="/" label="Etusivu" icon={<HomeIcon />} />
      {loggedIn && <MyNavLink to="/kirves" label="Pelit" icon={<GamesIcon />} />}
      {loggedIn && (
        <Grid
          className="site-header-logout"
          size={{
            md: "grow",
          }}
        >
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              alignItems: "center",
              gap: 1,
            }}
          >
            {playerName && (
              <Typography
                component="span"
                noWrap
                title={playerName}
                data-testid="header-player-name"
                sx={{ maxWidth: { xs: 120, sm: 280 } }}
              >
                {playerName}
              </Typography>
            )}
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

const HeaderContainer = () => {
  const { status, user, logout } = useAuth()
  return (
    <Header
      loggedIn={status === "authenticated"}
      playerName={user?.nickname || user?.email}
      logout={logout}
    />
  )
}

export default HeaderContainer
