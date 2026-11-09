import { propOr } from "ramda"

import translations from "./translations.json"

const translate = key => propOr("***puuttuva käännös***", key)(translations)

export default translate
