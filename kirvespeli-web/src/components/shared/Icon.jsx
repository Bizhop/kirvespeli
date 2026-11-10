import React from "react"
import { Tooltip } from "@mui/material"
import dealer from "url:./icons/jakaja.png"
import keep from "url:./icons/paalta.png"
import change from "url:./icons/varjays.png"
import pass from "url:./icons/viisi.png"
import axe from "url:./icons/kirves-valk.png"
import turn from "url:./icons/turn.svg"
import declared from "url:./icons/declared.svg"

export const icons = {
  dealer: [dealer, "Jakaja"],
  KEEP: [keep, "Päältä"],
  CHANGE: [change, "Värjäisin"],
  PASS: [pass, "Viitenä"],
  axe: [axe, "Kirves"],
  turn: [turn, "Vuorossa"],
  declared: [declared, "Pelaaja"],
}
const Icon = ({ name, size = 24, decorative = false }) => {
  const entry = icons[name]
  if (!entry) return null
  const image = (
    <img
      className="status-icon"
      src={entry[0]}
      alt={decorative ? "" : entry[1]}
      aria-hidden={decorative || undefined}
      width={size}
      height={size}
    />
  )
  return decorative ? image : <Tooltip title={entry[1]}>{image}</Tooltip>
}
export default Icon
