import React from "react"
import { HashRouter as Router, Route, Routes } from "react-router"
import { Provider } from "react-redux"
import { CssBaseline, ThemeProvider, Container, Box } from "@mui/material"
import createCache from "@emotion/cache"
import { CacheProvider } from "@emotion/react"
import theme from "../theme"
import { GoogleOAuthProvider } from "@react-oauth/google"

import Header from "./shared/Header"
import DashContainer from "./dash/DashContainer"
import KirvesContainer from "./kirves/KirvesContainer"
import KirvesGame from "./kirves/KirvesGame"
import Store from "../Store"

const store = Store()
const emotionCache = createCache({ key: "css" })

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
          <Router>
            <CssBaseline />
            <Container maxWidth="xl">
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
