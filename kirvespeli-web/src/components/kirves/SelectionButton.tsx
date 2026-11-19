import type React from "react"
import { Button, Tooltip } from "@mui/material"

// Shared appearance and selection feedback for speech and suit choices.
const SelectionButton = ({
  label,
  onClick,
  children,
  disabled,
}: {
  label: string
  onClick: () => void
  children: React.ReactNode
  disabled?: boolean
}) => (
  <Tooltip title={label}>
    <Button
      variant="text"
      aria-label={label}
      onClick={onClick}
      disabled={disabled}
      sx={{
        minWidth: 72,
        height: 72,
        p: 1,
        bgcolor: "transparent",
        color: "text.primary",
        "&:hover": { bgcolor: "transparent" },
        "& .status-icon": {
          transformOrigin: "50% 80%",
          translate: "0",
          rotate: "0deg",
          scale: "1",
          transition: [
            "translate 240ms cubic-bezier(0.2, 0.8, 0.2, 1.3)",
            "rotate 240ms cubic-bezier(0.2, 0.8, 0.2, 1.3)",
            "scale 240ms cubic-bezier(0.2, 0.8, 0.2, 1.3)",
          ].join(", "),
        },
        "@media (hover: hover)": {
          "&:hover .status-icon": { translate: "0 -5px", scale: "1.12", rotate: "-4deg" },
        },
        "&.Mui-focusVisible .status-icon": {
          translate: "0 -5px",
          scale: "1.12",
          rotate: "-4deg",
        },
        "&:active .status-icon": { translate: "0", rotate: "0deg", scale: "1.02" },
        "@media (prefers-reduced-motion: reduce)": {
          "& .status-icon": { transition: "none" },
          "&:hover .status-icon, &.Mui-focusVisible .status-icon": {
            translate: "0",
            rotate: "0deg",
            scale: "1.06",
          },
        },
        "&.Mui-focusVisible": { outline: "2px solid #fff3bd", outlineOffset: 2 },
      }}
    >
      {children}
    </Button>
  </Tooltip>
)
export default SelectionButton
