import translations from "./translations.json"

const translate = key => translations[key] ?? "***puuttuva käännös***"

export default translate
