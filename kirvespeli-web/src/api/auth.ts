import type { User } from "../types/kirves.ts"
import { request } from "./http.ts"

/** Exchanges a Google credential for the backend user (including `jwt`). */
export const login = (credential: string, signal?: AbortSignal): Promise<User> =>
  request<User>("GET", "auth", { token: credential, signal })

/** Validates a stored token; the response may carry a refreshed `jwt`. */
export const restore = (token: string, signal?: AbortSignal): Promise<User> =>
  request<User>("GET", "auth", { token, signal })

export const updateUser = (input: { nickname: string }, signal?: AbortSignal): Promise<User> =>
  request<User>("PUT", "user", { body: input, signal })
