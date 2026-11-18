import { createContext, useEffect, useMemo, useState, useSyncExternalStore } from "react"
import type { ReactNode } from "react"
import * as api from "../api/auth.ts"
import { clearToken, getToken, setToken } from "./token.ts"
import { createSession } from "./session.ts"
import type { AuthState } from "./session.ts"

export interface AuthApi extends AuthState {
  gameRequest<T>(operation: (signal: AbortSignal) => Promise<T>, signal?: AbortSignal): Promise<T>
  login(credential: string): Promise<void>
  loginFailed(error?: unknown): void
  logout(): void
  updateUser(input: { nickname: string }): Promise<void>
}

export const AuthContext = createContext<AuthApi | null>(null)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [session] = useState(() =>
    createSession({
      api,
      tokens: { get: getToken, set: setToken, clear: clearToken },
    }),
  )
  const state = useSyncExternalStore(session.subscribe, session.getState)

  useEffect(() => {
    void session.restore()
    return () => session.dispose()
  }, [session])

  const value = useMemo<AuthApi>(
    () => ({
      ...state,
      gameRequest: session.gameRequest,
      login: session.login,
      loginFailed: session.loginFailed,
      logout: session.logout,
      updateUser: session.updateUser,
    }),
    [state, session],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
