import { useContext } from "react"
import { AuthContext } from "./AuthProvider.tsx"
import type { AuthApi } from "./AuthProvider.tsx"

export const useAuth = (): AuthApi => {
  const auth = useContext(AuthContext)
  if (!auth) {
    throw new Error("useAuth must be used inside AuthProvider")
  }
  return auth
}
