import React from "react"
import { MemoryRouter } from "react-router-dom"

import "../src/styles/style.css"

export const decorators = [
  Story => (
    <MemoryRouter initialEntries={["/"]}>
      <Story />
    </MemoryRouter>
  ),
]
