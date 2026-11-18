import type { ReactNode } from "react"

export interface PreviewPage {
  title: string
  group: "Components" | "Containers"
  variants: Record<string, () => ReactNode>
}
export interface PreviewAction {
  name: string
  args: unknown[]
}
declare global {
  interface WindowEventMap {
    "preview-action": CustomEvent<PreviewAction>
  }
}
