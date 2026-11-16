import { test } from "node:test"
import assert from "node:assert/strict"
import * as api from "../src/api/kirves.ts"
import * as auth from "../src/api/auth.ts"

const recorded = async (run: () => Promise<unknown>) => {
  const original = globalThis.fetch
  let url = ""
  let init: RequestInit = {}
  globalThis.fetch = ((u: string, i: RequestInit) => {
    url = u
    init = i
    return Promise.resolve(new Response(null, { status: 200 }))
  }) as typeof fetch
  try {
    await run()
  } finally {
    globalThis.fetch = original
  }
  const headers = init.headers as Record<string, string>
  return {
    path: new URL(url, "http://x.test").pathname,
    method: init.method,
    body: init.body,
    headers,
  }
}

const replay = { gameId: 3, handId: 4, index: 5 }
const cases: [string, () => Promise<unknown>, string, string, unknown?][] = [
  ["createGame", () => api.createGame(), "POST", "/api/kirves"],
  ["listGames", () => api.listGames(), "GET", "/api/kirves"],
  ["joinGame", () => api.joinGame(3), "POST", "/api/kirves/3"],
  ["getGame", () => api.getGame(3), "GET", "/api/kirves/3"],
  ["deleteGame", () => api.deleteGame(3), "DELETE", "/api/kirves/3"],
  [
    "sendAction",
    () => api.sendAction(3, { action: "PLAY_CARD", index: 2 }),
    "PUT",
    "/api/kirves/3",
    '{"action":"PLAY_CARD","index":2}',
  ],
  ["getLog", () => api.getLog(3, 4), "GET", "/api/kirves/3/4"],
  ["getReplay", () => api.getReplay(replay), "GET", "/api/kirves/3/4/5"],
  ["restoreGame", () => api.restoreGame(replay), "POST", "/api/kirves/3/4/5"],
  [
    "updateUser",
    () => auth.updateUser({ nickname: "Ville" }),
    "PUT",
    "/api/user",
    '{"nickname":"Ville"}',
  ],
  ["login", () => auth.login("cred"), "GET", "/api/auth"],
]

for (const [name, run, method, path, body] of cases) {
  test(`${name}: ${method} ${path}`, async () => {
    const r = await recorded(run)
    assert.deepStrictEqual(r.method, method)
    assert.deepStrictEqual(r.path, path)
    assert.deepStrictEqual(r.body, body)
  })
}

test("login and restore send the given string as Authorization", async () => {
  assert.deepStrictEqual(
    (await recorded(() => auth.login("google-cred"))).headers["Authorization"],
    "google-cred",
  )
  assert.deepStrictEqual(
    (await recorded(() => auth.restore("JWT t"))).headers["Authorization"],
    "JWT t",
  )
})
