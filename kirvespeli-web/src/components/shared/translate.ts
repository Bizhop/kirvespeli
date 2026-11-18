import translations from "./translations.json" with { type: "json" }

const translate = (key: string | null | undefined) =>
  translations[key as keyof typeof translations] ?? "***puuttuva käännös***"

export default translate
