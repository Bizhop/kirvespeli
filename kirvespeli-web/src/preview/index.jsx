import React, { useEffect, useState } from "react"
import { createRoot } from "react-dom/client"
import { MemoryRouter } from "react-router"

import pages from "./pages"
import { CssBaseline, Tab, Tabs, ThemeProvider } from "@mui/material"
import theme from "../theme"

const groups = [...new Set(pages.map(page => page.group))]

const pageIdFromHash = () => decodeURIComponent(window.location.hash.slice(1))

// Remembered per browser; storage may be unavailable (private mode), then it is per session.
const usePersistentFlag = name => {
  const key = `preview-${name}`
  const [value, setValue] = useState(() => {
    try {
      return window.localStorage.getItem(key) === "true"
    } catch {
      return false
    }
  })
  const set = next => {
    setValue(next)
    try {
      window.localStorage.setItem(key, String(next))
    } catch {}
  }
  return [value, set]
}

// Small fixed button that restores a hidden panel.
const Handle = ({ side, label, onClick, children }) => (
  <button
    type="button"
    className={`preview-handle preview-handle-${side}`}
    aria-label={label}
    title={label}
    onClick={onClick}
  >
    {children}
  </button>
)

const Nav = ({ selectedId, onHide }) => (
  <nav className="preview-nav">
    <button
      type="button"
      className="preview-hide"
      aria-label="Piilota valikko"
      title="Piilota valikko"
      onClick={onHide}
    >
      ‹
    </button>
    {groups.map(group => (
      <div key={group}>
        <h2>{group}</h2>
        {pages
          .filter(page => page.group === group)
          .map(page => (
            <a
              key={page.title}
              href={`#${encodeURIComponent(page.title)}`}
              className={page.title === selectedId ? "selected" : undefined}
            >
              {page.title}
            </a>
          ))}
      </div>
    ))}
  </nav>
)

const Page = ({ page }) => {
  const names = Object.keys(page.variants)
  const [selected, setSelected] = useState(names[0])
  const [tabsHidden, setTabsHidden] = usePersistentFlag("tabs-hidden")
  const index = names.indexOf(selected)
  return (
    <>
      {tabsHidden ? (
        <Handle side="right" label="Näytä välilehdet" onClick={() => setTabsHidden(false)}>
          ▾ {selected}
        </Handle>
      ) : (
        <div className="preview-header">
          <h1>{page.title}</h1>
          <button
            type="button"
            className="preview-hide"
            aria-label="Piilota välilehdet"
            title="Piilota välilehdet"
            onClick={() => setTabsHidden(true)}
          >
            ▴
          </button>
        </div>
      )}
      <Tabs
        sx={tabsHidden ? { display: "none" } : undefined}
        className="preview-variant-tabs"
        value={selected}
        onChange={(_, name) => setSelected(name)}
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
        aria-label={`${page.title} variants`}
      >
        {names.map((name, i) => (
          <Tab
            key={name}
            value={name}
            label={name}
            id={`preview-variant-tab-${i}`}
            aria-controls={`preview-variant-panel-${i}`}
          />
        ))}
      </Tabs>
      <section
        role="tabpanel"
        id={`preview-variant-panel-${index}`}
        aria-labelledby={`preview-variant-tab-${index}`}
      >
        <MemoryRouter key={selected} initialEntries={["/"]}>
          {page.variants[selected]()}
        </MemoryRouter>
      </section>
    </>
  )
}

const ActionBar = () => {
  const [last, setLast] = useState(null)
  useEffect(() => {
    const listener = event => setLast(event.detail)
    window.addEventListener("preview-action", listener)
    return () => window.removeEventListener("preview-action", listener)
  }, [])
  if (!last) return null
  return (
    <div className="preview-action-bar">
      {last.name} {last.args.map(arg => JSON.stringify(arg)).join(", ")}
    </div>
  )
}

const Preview = () => {
  const [selectedId, setSelectedId] = useState(pageIdFromHash)
  useEffect(() => {
    const listener = () => setSelectedId(pageIdFromHash())
    window.addEventListener("hashchange", listener)
    return () => window.removeEventListener("hashchange", listener)
  }, [])
  const page = pages.find(p => p.title === selectedId)
  const [navHidden, setNavHidden] = usePersistentFlag("nav-hidden")
  return (
    <div className="preview-layout">
      {navHidden ? (
        <Handle side="left" label="Näytä valikko" onClick={() => setNavHidden(false)}>
          ☰
        </Handle>
      ) : (
        <Nav selectedId={selectedId} onHide={() => setNavHidden(true)} />
      )}
      <main className="preview-main">
        {page ? <Page key={page.title} page={page} /> : <p>Valitse komponentti.</p>}
      </main>
      <ActionBar />
    </div>
  )
}

createRoot(document.getElementById("preview")).render(
  <ThemeProvider theme={theme}>
    <CssBaseline />
    <Preview />
  </ThemeProvider>,
)
