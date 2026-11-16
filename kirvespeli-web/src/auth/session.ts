import type { User } from "../types/kirves.ts"
import { ApiError, isAbortError } from "../api/http.ts"

export type AuthStatus = "restoring" | "anonymous" | "authenticated"

export interface AuthState {
  status: AuthStatus
  /** Never keeps the jwt; the token lives only in the token store. */
  user: Omit<User, "jwt"> | null
  loginError: string | null
  loggingIn: boolean
  updateError: string | null
  updating: boolean
}

export interface SessionDeps {
  api: {
    login(credential: string, signal?: AbortSignal): Promise<User>
    restore(token: string, signal?: AbortSignal): Promise<User>
    updateUser(input: { nickname: string }, signal?: AbortSignal): Promise<User>
  }
  tokens: {
    get(): string | null
    set(token: string): void
    clear(): void
  }
}

export interface Session {
  getState(): AuthState
  subscribe(listener: () => void): () => void
  restore(): Promise<void>
  login(credential: string): Promise<void>
  loginFailed(error?: unknown): void
  logout(): void
  updateUser(input: { nickname: string }): Promise<void>
  dispose(): void
}

const LOGIN_FALLBACK = "Kirjautuminen epäonnistui"
const NETWORK_MESSAGE = "Yhteys palvelimeen epäonnistui"
const UPDATE_FALLBACK = "Käyttäjän tallennus epäonnistui"

const publicUser = (user: User): Omit<User, "jwt"> => ({
  email: user.email,
  nickname: user.nickname,
})

const backendMessage = (e: unknown): string | null => {
  if (e instanceof ApiError && e.kind === "http") {
    const body = e.body
    if (body && typeof body === "object" && "message" in body) {
      const message = (body as { message: unknown }).message
      if (typeof message === "string" && message.length > 0) {
        return message
      }
    }
    if (typeof body === "string" && body.trim().length > 0) {
      return body
    }
  }
  return null
}

const loginMessage = (e: unknown): string =>
  backendMessage(e) ??
  (e instanceof ApiError && e.kind === "network" ? NETWORK_MESSAGE : LOGIN_FALLBACK)

const anonymousState: AuthState = {
  status: "anonymous",
  user: null,
  loginError: null,
  loggingIn: false,
  updateError: null,
  updating: false,
}

export const createSession = ({ api, tokens }: SessionDeps): Session => {
  let state: AuthState = {
    ...anonymousState,
    status: tokens.get() ? "restoring" : "anonymous",
  }
  const listeners = new Set<() => void>()
  let generation = 0
  let restoreController: AbortController | null = null
  let loginController: AbortController | null = null
  let updateController: AbortController | null = null

  const setState = (patch: Partial<AuthState>) => {
    state = { ...state, ...patch }
    listeners.forEach(listener => listener())
  }

  // Supersedes everything pending: late responses see a different generation and are dropped.
  const supersede = () => {
    generation += 1
    restoreController?.abort()
    loginController?.abort()
    updateController?.abort()
    restoreController = null
    loginController = null
    updateController = null
  }

  const endSession = (patch: Partial<AuthState> = {}) => {
    supersede()
    tokens.clear()
    setState({ ...anonymousState, ...patch })
  }

  const restore = async () => {
    if (state.status !== "restoring" || restoreController) {
      return
    }
    const token = tokens.get()
    if (!token) {
      setState({ status: "anonymous" })
      return
    }
    const controller = new AbortController()
    restoreController = controller
    const started = generation
    try {
      const user = await api.restore(token, controller.signal)
      if (started !== generation) {
        return
      }
      supersede()
      if (user.jwt) {
        tokens.set(user.jwt)
      }
      setState({ status: "authenticated", user: publicUser(user) })
    } catch (e) {
      if (started !== generation || isAbortError(e)) {
        return
      }
      // expired or invalid token: silent fall back to the login screen
      endSession()
    }
  }

  const login = async (credential: string) => {
    supersede()
    const controller = new AbortController()
    loginController = controller
    const started = generation
    setState({ loggingIn: true, loginError: null })
    try {
      const user = await api.login(credential, controller.signal)
      if (started !== generation) {
        return
      }
      if (!user.jwt) {
        endSession({ loginError: LOGIN_FALLBACK })
        return
      }
      tokens.set(user.jwt)
      loginController = null
      setState({
        status: "authenticated",
        user: publicUser(user),
        loggingIn: false,
        loginError: null,
        updateError: null,
      })
    } catch (e) {
      if (started !== generation || isAbortError(e)) {
        return
      }
      loginController = null
      const loginError = loginMessage(e)
      if (state.status === "authenticated") {
        setState({ loggingIn: false, loginError })
      } else {
        // a stale token (or an aborted restore) must not keep the app in a half state
        tokens.clear()
        setState({ status: "anonymous", user: null, loggingIn: false, loginError })
      }
    }
  }

  const loginFailed = (_error?: unknown) => {
    endSession({ loginError: LOGIN_FALLBACK })
  }

  const logout = () => {
    endSession()
  }

  const updateUser = async (input: { nickname: string }) => {
    if (state.status !== "authenticated") {
      return
    }
    updateController?.abort()
    const controller = new AbortController()
    updateController = controller
    const started = generation
    setState({ updating: true, updateError: null })
    try {
      const user = await api.updateUser(input, controller.signal)
      if (started !== generation) {
        return
      }
      if (user.jwt) {
        tokens.set(user.jwt)
      }
      updateController = null
      setState({ user: publicUser(user), updating: false, updateError: null })
    } catch (e) {
      if (started !== generation || isAbortError(e)) {
        return
      }
      if (e instanceof ApiError && e.kind === "http" && e.status === 401) {
        // auth-owned call: 401 means the token is no longer valid
        endSession()
        return
      }
      updateController = null
      setState({ updating: false, updateError: backendMessage(e) ?? UPDATE_FALLBACK })
    }
  }

  const dispose = () => {
    supersede()
    if (state.loggingIn || state.updating) {
      setState({ loggingIn: false, updating: false })
    }
  }

  return {
    getState: () => state,
    subscribe: listener => {
      listeners.add(listener)
      return () => {
        listeners.delete(listener)
      }
    },
    restore,
    login,
    loginFailed,
    logout,
    updateUser,
    dispose,
  }
}
