// The only module that touches the stored session token.
// The value is the backend's `JWT ...` string, sent verbatim.
const KEY = "kirves-token"

export const getToken = (): string | null => {
  try {
    return localStorage.getItem(KEY)
  } catch {
    return null
  }
}

export const setToken = (token: string): void => {
  try {
    localStorage.setItem(KEY, token)
  } catch {
    // storage unavailable: the session lasts until reload
  }
}

export const clearToken = (): void => {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // storage unavailable
  }
}
