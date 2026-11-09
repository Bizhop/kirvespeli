import React from "react"
import { Route, Routes } from "react-router-dom"
import { Provider } from "react-redux"
import { HashRouter as Router } from "react-router-dom"
import { CssBaseline, createTheme, ThemeProvider, Container, Box } from "@mui/material"
import createCache from "@emotion/cache"
import { CacheProvider } from "@emotion/react"
import { red } from "@mui/material/colors"
import { GoogleOAuthProvider } from "@react-oauth/google"

import Header from "./shared/Header"
import DashContainer from "./dash/DashContainer"
import KirvesContainer from "./kirves/KirvesContainer"
import KirvesGame from "./kirves/KirvesGame"
import Store from "../Store"

const store = Store()
const emotionCache = createCache({ key: "css" })
const theme = createTheme({
  palette: {
    primary: {
      main: "#556cd6",
    },
    secondary: {
      main: "#19857b",
    },
    error: {
      main: red.A700,
    },
    black: {
      main: "#000000",
    },
  },
})

const NotFound = () => (
  <Box sx={{ flexGrow: 1 }}>
    <h1>Page not Found 404!</h1>
  </Box>
)

const MyRoutes = () => (
  <Routes>
    <Route path="/" element={<DashContainer />} />
    <Route path="/kirves" element={<KirvesContainer />} />
    <Route path="/kirves/:id" element={<KirvesGame />} />
    <Route path="*" element={<NotFound />} />
  </Routes>
)

const App = () => (
  <Provider store={store}>
    <CacheProvider value={emotionCache}>
      <ThemeProvider theme={theme}>
        <GoogleOAuthProvider clientId="266150004835-ijh2ibgdsa9timu7cmak0udc76hnhldb.apps.googleusercontent.com">
          <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
            <CssBaseline />
            <Container>
              <Header />
              <MyRoutes />
            </Container>
          </Router>
        </GoogleOAuthProvider>
      </ThemeProvider>
    </CacheProvider>
  </Provider>
)

export default App
