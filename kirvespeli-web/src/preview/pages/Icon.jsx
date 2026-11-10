import React from "react"
import Icon, { icons } from "../../components/shared/Icon"
export default {
  title: "Icon",
  group: "Components",
  variants: Object.fromEntries(
    Object.entries(icons).map(([name, [, label]]) => [
      name,
      () => (
        <div>
          <Icon name={name} size={48} /> {label}
        </div>
      ),
    ]),
  ),
}
