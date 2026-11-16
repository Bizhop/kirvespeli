import { getToken } from "../auth/token.ts"

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE"

export interface RequestOptions {
  body?: unknown
  signal?: AbortSignal
  /** Authorization value, sent verbatim. Defaults to the stored token; omitted when empty. */
  token?: string | null
  /** API origin without trailing slash. Defaults to VITE_API_URL. */
  baseUrl?: string
}

export class ApiError extends Error {
  kind: "http" | "network"
  status: number
  body: unknown

  constructor(kind: "http" | "network", status: number, message: string, body: unknown = null) {
    super(message)
    this.name = "ApiError"
    this.kind = kind
    this.status = status
    this.body = body
  }
}

export const isAbortError = (e: unknown): boolean => e instanceof Error && e.name === "AbortError"

export const apiUrl = (
  path: string,
  baseUrl: string = import.meta.env?.VITE_API_URL ?? "",
): string => `${baseUrl.replace(/\/+$/, "")}/api/${path.replace(/^\/+/, "")}`

const HTTP_FALLBACK = "Pyyntö epäonnistui"
const NETWORK_FALLBACK = "Yhteys palvelimeen epäonnistui"

const parseBody = async (response: Response): Promise<unknown> => {
  const text = await response.text()
  if (text.length === 0) {
    return undefined
  }
  if ((response.headers.get("content-type") ?? "").includes("json")) {
    try {
      return JSON.parse(text)
    } catch {
      return text
    }
  }
  return text
}

const errorMessage = (body: unknown): string => {
  if (body && typeof body === "object" && "message" in body) {
    const message = (body as { message: unknown }).message
    if (typeof message === "string" && message.length > 0) {
      return message
    }
  }
  if (typeof body === "string" && body.trim().length > 0) {
    return body
  }
  return HTTP_FALLBACK
}

export const request = async <T = void>(
  method: HttpMethod,
  path: string,
  options: RequestOptions = {},
): Promise<T> => {
  const token = options.token === undefined ? getToken() : options.token
  const headers: Record<string, string> = {}
  if (token) {
    headers["Authorization"] = token
  }
  let body: string | undefined
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json"
    body = JSON.stringify(options.body)
  }

  const url = apiUrl(path, options.baseUrl)
  let response: Response
  try {
    response = await fetch(url, {
      method,
      headers,
      body,
      signal: options.signal,
    })
  } catch (e) {
    if (isAbortError(e)) {
      throw e
    }
    throw new ApiError("network", 0, NETWORK_FALLBACK)
  }

  let parsed: unknown
  try {
    parsed = await parseBody(response)
  } catch (e) {
    if (isAbortError(e)) {
      throw e
    }
    throw new ApiError("network", 0, NETWORK_FALLBACK)
  }
  if (!response.ok) {
    throw new ApiError("http", response.status, errorMessage(parsed), parsed ?? null)
  }
  return parsed as T
}
