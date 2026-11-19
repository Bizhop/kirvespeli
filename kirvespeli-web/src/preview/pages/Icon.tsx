import type { PreviewPage } from "../types.ts"
import Icon, { icons } from "../../components/shared/Icon.tsx"
export default {
  title: "Icon",
  group: "Components",
  variants: Object.fromEntries(
    Object.entries(icons).map(([name, [, label]]) => [
      name,
      () => (
        <div>
          <Icon name={name as keyof typeof icons} size={48} /> {label}
        </div>
      ),
    ]),
  ),
} satisfies PreviewPage
