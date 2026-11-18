import type React from "react"

import svgImages from "./svg/svgImages.ts"

export const SvgImage = ({
  name,
  className,
  width,
  height,
  ...props
}: Omit<React.ComponentProps<"img">, "name"> & { name?: keyof typeof svgImages | null }) => {
  if (!name) return null
  const defaultSize = !className && !width && !height
  return (
    <img
      {...props}
      className={className}
      alt={props.alt ?? name}
      width={width ?? (defaultSize ? 140 : undefined)}
      height={height ?? (defaultSize ? 200 : undefined)}
      src={svgImages[name]}
    />
  )
}
