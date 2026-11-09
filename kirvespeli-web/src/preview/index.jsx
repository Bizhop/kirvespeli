import React, { useEffect, useState } from "react"
import { createRoot } from "react-dom/client"
import { MemoryRouter } from "react-router"

import pages from "./pages"

const groups = [...new Set(pages.map(page => page.group))]

const pageIdFromHash = () => decodeURIComponent(window.location.hash.slice(1))

const Nav = ({ selectedId }) => (
  <nav className="preview-nav">
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

const Page = ({ page }) => (
  <>
    <h1>{page.title}</h1>
    {Object.entries(page.variants).map(([name, render]) => (
      <section key={name}>
        <h3 className="preview-variant-title">{name}</h3>
        <MemoryRouter initialEntries={["/"]}>{render()}</MemoryRouter>
      </section>
    ))}
  </>
)

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
  return (
    <div className="preview-layout">
      <Nav selectedId={selectedId} />
      <main className="preview-main">
        {page ? <Page key={page.title} page={page} /> : <p>Valitse komponentti.</p>}
      </main>
      <ActionBar />
    </div>
  )
}

createRoot(document.getElementById("preview")).render(<Preview />)
