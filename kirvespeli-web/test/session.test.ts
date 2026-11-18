import { test } from "node:test"
import assert from "node:assert/strict"
import { createSession } from "../src/auth/session.ts"
import { ApiError } from "../src/api/http.ts"
import type { User } from "../src/types/kirves.ts"

interface Deferred<T> {
  promise: Promise<T>
  resolve(value: T): void
  reject(error: unknown): void
}
const deferred = <T>(): Deferred<T> => {
  let resolve!: (value: T) => void
  let reject!: (error: unknown) => void
  const promise = new Promise<T>((res, rej) => {
    resolve = res
    reject = rej
  })
  return { promise, resolve, reject }
}
const tick = () => new Promise(resolve => setTimeout(resolve, 0))

const ville: User = { email: "v@x.fi", nickname: "Ville", jwt: "JWT new" }

const setup = (stored: string | null = null) => {
  let token = stored
  const calls = { login: 0, restore: 0, update: 0 }
  const pending = {
    login: [] as Deferred<User>[],
    restore: [] as Deferred<User>[],
    update: [] as Deferred<User>[],
  }
  const signals: AbortSignal[] = []
  const make = (kind: keyof typeof pending) => (_arg: unknown, signal?: AbortSignal) => {
    calls[kind] += 1
    const d = deferred<User>()
    pending[kind].push(d)
    if (signal) {
      signals.push(signal)
      signal.addEventListener("abort", () => d.reject(new DOMException("aborted", "AbortError")))
    }
    return d.promise
  }
  const session = createSession({
    api: { login: make("login"), restore: make("restore"), updateUser: make("update") },
    tokens: {
      get: () => token,
      set: t => {
        token = t
      },
      clear: () => {
        token = null
      },
    },
  })
  return { session, calls, pending, signals, token: () => token }
}

const http = (status: number, body: unknown = null) => new ApiError("http", status, "msg", body)

test("no stored token: anonymous and no request", async () => {
  const t = setup()
  await t.session.restore()
  assert.equal(t.session.getState().status, "anonymous")
  assert.equal(t.calls.restore, 0)
})

test("restore success keeps the stored token when no jwt is returned", async () => {
  const t = setup("JWT old")
  assert.equal(t.session.getState().status, "restoring")
  const done = t.session.restore()
  t.pending.restore[0].resolve({ email: "v@x.fi", nickname: "Ville" })
  await done
  assert.equal(t.session.getState().status, "authenticated")
  assert.equal(t.token(), "JWT old")
})

test("restore success replaces the token and never keeps the jwt in state", async () => {
  const t = setup("JWT old")
  const done = t.session.restore()
  t.pending.restore[0].resolve(ville)
  await done
  assert.equal(t.token(), "JWT new")
  assert.deepEqual(t.session.getState().user, { email: "v@x.fi", nickname: "Ville" })
})

test("restore is idempotent while in flight", async () => {
  const t = setup("JWT old")
  const a = t.session.restore()
  const b = t.session.restore()
  assert.equal(t.calls.restore, 1)
  t.pending.restore[0].resolve(ville)
  await Promise.all([a, b])
})

test("restore failure clears the token silently", async () => {
  const t = setup("JWT old")
  const done = t.session.restore()
  t.pending.restore[0].reject(http(403))
  await done
  const s = t.session.getState()
  assert.equal(s.status, "anonymous")
  assert.equal(s.loginError, null)
  assert.equal(t.token(), null)
})

test("failed login shows the backend message or a fallback", async () => {
  const t = setup()
  const first = t.session.login("cred")
  assert.equal(t.session.getState().loggingIn, true)
  t.pending.login[0].reject(http(403))
  await first
  assert.equal(t.session.getState().loginError, "Kirjautuminen epäonnistui")
  assert.equal(t.session.getState().loggingIn, false)
  const second = t.session.login("cred")
  assert.equal(t.session.getState().loginError, null)
  t.pending.login[1].reject(new ApiError("network", 0, "x"))
  await second
  assert.equal(t.session.getState().loginError, "Yhteys palvelimeen epäonnistui")
})

test("successful login stores the jwt", async () => {
  const t = setup()
  const done = t.session.login("cred")
  t.pending.login[0].resolve(ville)
  await done
  assert.equal(t.session.getState().status, "authenticated")
  assert.equal(t.token(), "JWT new")
})

test("login response without jwt is a failure", async () => {
  const t = setup()
  const done = t.session.login("cred")
  t.pending.login[0].resolve({ email: "v@x.fi", nickname: null })
  await done
  const s = t.session.getState()
  assert.equal(s.status, "anonymous")
  assert.equal(s.user, null)
  assert.ok(s.loginError)
  assert.equal(t.token(), null)
})

test("failed login while restoring ends up anonymous", async () => {
  const t = setup("JWT old")
  const restoring = t.session.restore()
  const done = t.session.login("cred")
  await restoring
  t.pending.login[0].reject(http(403))
  await done
  assert.equal(t.session.getState().status, "anonymous")
  assert.equal(t.token(), null)
})

test("logout is not undone by a late restore, login or update", async () => {
  const restoring = setup("JWT old")
  const r = restoring.session.restore()
  restoring.session.logout()
  restoring.pending.restore[0].resolve(ville)
  await r
  assert.equal(restoring.session.getState().status, "anonymous")
  assert.equal(restoring.token(), null)

  const loggingIn = setup()
  const l = loggingIn.session.login("cred")
  loggingIn.session.logout()
  loggingIn.pending.login[0].resolve(ville)
  await l
  assert.equal(loggingIn.session.getState().status, "anonymous")
  assert.equal(loggingIn.session.getState().loggingIn, false)
  assert.equal(loggingIn.token(), null)

  const updating = setup()
  const login = updating.session.login("cred")
  updating.pending.login[0].resolve(ville)
  await login
  const u = updating.session.updateUser({ nickname: "Uusi" })
  updating.session.logout()
  updating.pending.update[0].resolve({ email: "v@x.fi", nickname: "Uusi" })
  await u
  assert.equal(updating.session.getState().status, "anonymous")
  assert.equal(updating.session.getState().user, null)
})

test("superseded login: the first response resolving late is dropped", async () => {
  const t = setup()
  const first = t.session.login("one")
  const second = t.session.login("two")
  t.pending.login[1].resolve({ ...ville, email: "second@x.fi", jwt: "JWT two" })
  await second
  t.pending.login[0].resolve({ ...ville, email: "first@x.fi", jwt: "JWT one" })
  await first
  assert.equal(t.session.getState().user?.email, "second@x.fi")
  assert.equal(t.token(), "JWT two")
})

const authenticated = async () => {
  const t = setup()
  const login = t.session.login("cred")
  t.pending.login[0].resolve(ville)
  await login
  return t
}

test("update keeps the token when the response has no jwt", async () => {
  const t = await authenticated()
  const done = t.session.updateUser({ nickname: "Uusi" })
  assert.equal(t.session.getState().updating, true)
  t.pending.update[0].resolve({ email: "v@x.fi", nickname: "Uusi" })
  await done
  assert.equal(t.token(), "JWT new")
  assert.equal(t.session.getState().user?.nickname, "Uusi")
  assert.equal(t.session.getState().updating, false)
})

test("update stores a new jwt when returned", async () => {
  const t = await authenticated()
  const done = t.session.updateUser({ nickname: "Uusi" })
  t.pending.update[0].resolve({ ...ville, jwt: "JWT third" })
  await done
  assert.equal(t.token(), "JWT third")
})

test("update 400 shows the backend message and keeps the session", async () => {
  const t = await authenticated()
  const done = t.session.updateUser({ nickname: "x" })
  t.pending.update[0].reject(http(400, { message: "Nimi varattu" }))
  await done
  assert.equal(t.session.getState().updateError, "Nimi varattu")
  assert.equal(t.session.getState().status, "authenticated")
})

test("update 401 ends the session", async () => {
  const t = await authenticated()
  const done = t.session.updateUser({ nickname: "x" })
  t.pending.update[0].reject(http(401))
  await done
  assert.equal(t.session.getState().status, "anonymous")
  assert.equal(t.token(), null)
})

test("update transport error keeps the session with a fallback message", async () => {
  const t = await authenticated()
  const done = t.session.updateUser({ nickname: "x" })
  t.pending.update[0].reject(new ApiError("network", 0, "x"))
  await done
  assert.equal(t.session.getState().status, "authenticated")
  assert.equal(t.session.getState().updateError, "Käyttäjän tallennus epäonnistui")
  assert.equal(t.token(), "JWT new")
})

test("loginFailed clears the session and shows an error", async () => {
  const t = await authenticated()
  t.session.loginFailed(new Error("popup closed"))
  const s = t.session.getState()
  assert.equal(s.status, "anonymous")
  assert.ok(s.loginError)
  assert.equal(t.token(), null)
})

test("dispose stops commits and aborts requests", async () => {
  const t = setup("JWT old")
  const done = t.session.restore()
  t.session.dispose()
  assert.equal(t.signals[0].aborted, true)
  t.pending.restore[0].resolve(ville)
  await done
  await tick()
  assert.equal(t.session.getState().status, "restoring")
  assert.equal(t.token(), "JWT old")
})

test("AbortError never reaches the state", async () => {
  const t = setup()
  const first = t.session.login("one")
  const second = t.session.login("two")
  await first
  const s = t.session.getState()
  assert.equal(s.loginError, null)
  assert.equal(s.loggingIn, true)
  t.pending.login[1].resolve(ville)
  await second
})

test("subscribers are notified and can unsubscribe", async () => {
  const t = setup()
  let n = 0
  const off = t.session.subscribe(() => {
    n += 1
  })
  t.session.loginFailed()
  assert.equal(n, 1)
  off()
  t.session.loginFailed()
  assert.equal(n, 1)
})

test("game 401 ends the session and aborts all other pending game requests", async () => {
  const t = await authenticated()
  let signal!: AbortSignal
  const late = deferred<string>()
  const other = t.session.gameRequest(s => {
    signal = s
    return late.promise
  })
  const rejected = assert.rejects(other, { name: "AbortError" })
  await assert.rejects(
    t.session.gameRequest(async () => {
      throw http(401)
    }),
    ApiError,
  )
  assert.equal(t.session.getState().status, "anonymous")
  assert.equal(t.token(), null)
  assert.equal(signal.aborted, true)
  late.resolve("private data")
  await rejected
})

test("game permission, unknown-user and network errors keep the authenticated session", async () => {
  const t = await authenticated()
  for (const error of [http(403), http(400), new ApiError("network", 0, "offline")]) {
    await assert.rejects(
      t.session.gameRequest(async () => {
        throw error
      }),
      ApiError,
    )
    assert.equal(t.session.getState().status, "authenticated")
    assert.equal(t.token(), "JWT new")
  }
})

test("late game 401 after switching accounts cannot expire the new session", async () => {
  const t = await authenticated()
  const late = deferred<void>()
  const old = assert.rejects(
    t.session.gameRequest(() => late.promise),
    { name: "AbortError" },
  )
  t.session.logout()
  const login = t.session.login("other")
  t.pending.login[1].resolve({ ...ville, email: "other@x.fi", jwt: "JWT other" })
  await login
  late.reject(http(401))
  await old
  assert.equal(t.session.getState().user?.email, "other@x.fi")
  assert.equal(t.token(), "JWT other")
})

test("canceled page request cannot end the session or return late private data", async () => {
  const t = await authenticated()
  for (const fail of [false, true]) {
    const controller = new AbortController()
    const late = deferred<string>()
    const done = assert.rejects(
      t.session.gameRequest(() => late.promise, controller.signal),
      { name: "AbortError" },
    )
    controller.abort()
    if (fail) late.reject(http(401))
    else late.resolve("old route")
    await done
    assert.equal(t.session.getState().status, "authenticated")
  }
})

test("game calls do not start while restoring or anonymous", async () => {
  for (const token of [null, "JWT old"]) {
    const t = setup(token)
    let called = false
    await assert.rejects(
      t.session.gameRequest(async () => {
        called = true
      }),
      { name: "AbortError" },
    )
    assert.equal(called, false)
  }
})

test("re-authentication as the same user changes the page session key", async () => {
  const t = await authenticated()
  const before = t.session.getState().sessionKey
  const login = t.session.login("new credential")
  assert.ok(t.session.getState().sessionKey > before)
  let called = false
  await assert.rejects(
    t.session.gameRequest(async () => {
      called = true
    }),
    { name: "AbortError" },
  )
  assert.equal(called, false)
  t.pending.login[1].resolve(ville)
  await login
  assert.equal(t.session.getState().user?.email, ville.email)
  assert.ok(t.session.getState().sessionKey > before)
  assert.equal(await t.session.gameRequest(async () => "new session"), "new session")
})
