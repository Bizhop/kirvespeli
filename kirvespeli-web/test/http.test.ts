import { test } from "node:test"
import assert from "node:assert/strict"
import { ApiError, apiUrl, isAbortError, request } from "../src/api/http.ts"

type Call = { url: string; init: RequestInit }

const rejection = async (run: () => Promise<unknown>): Promise<ApiError> => {
  try {
    await run()
  } catch (e) {
    return e as ApiError
  }
  throw new Error("expected rejection")
}

const withFetch = async (
  impl: (url: string, init: RequestInit) => Promise<Response>,
  run: (calls: Call[]) => Promise<void>,
) => {
  const original = globalThis.fetch
  const calls: Call[] = []
  globalThis.fetch = ((url: string, init: RequestInit) => {
    calls.push({ url, init })
    return impl(url, init)
  }) as typeof fetch
  try {
    await run(calls)
  } finally {
    globalThis.fetch = original
  }
}

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { "content-type": "application/json" } })
const base = "http://x.test"

test("apiUrl joins base and path with single slashes", () => {
  assert.deepStrictEqual(apiUrl("kirves/1", "http://x.test"), "http://x.test/api/kirves/1")
  assert.deepStrictEqual(apiUrl("/kirves", "http://x.test/"), "http://x.test/api/kirves")
})

test("sends the token verbatim and omits the header without one", async () => {
  await withFetch(
    () => Promise.resolve(json({})),
    async calls => {
      await request("GET", "a", { token: "JWT abc", baseUrl: base })
      await request("GET", "a", { token: null, baseUrl: base })
      assert.deepStrictEqual(
        (calls[0].init.headers as Record<string, string>)["Authorization"],
        "JWT abc",
      )
      assert(!("Authorization" in (calls[1].init.headers as Record<string, string>)))
    },
  )
})

test("JSON body sets content type; no body sets none", async () => {
  await withFetch(
    () => Promise.resolve(json({})),
    async calls => {
      await request("PUT", "a", { body: { n: 1 }, token: null, baseUrl: base })
      await request("POST", "a", { token: null, baseUrl: base })
      const h0 = calls[0].init.headers as Record<string, string>
      assert.deepStrictEqual(h0["Content-Type"], "application/json")
      assert.deepStrictEqual(calls[0].init.body, '{"n":1}')
      assert.deepStrictEqual(calls[0].init.method, "PUT")
      assert(!("Content-Type" in (calls[1].init.headers as Record<string, string>)))
      assert.deepStrictEqual(calls[1].init.body, undefined)
    },
  )
})

test("empty success bodies resolve undefined", async () => {
  for (const status of [200, 202, 204]) {
    await withFetch(
      () => Promise.resolve(new Response(null, { status })),
      async () => {
        assert.deepStrictEqual(
          await request("POST", "a", { token: null, baseUrl: base }),
          undefined,
        )
      },
    )
  }
})

test("parses JSON and falls back to text", async () => {
  await withFetch(
    () => Promise.resolve(json({ a: 1 })),
    async () =>
      assert.deepStrictEqual(await request("GET", "a", { token: null, baseUrl: base }), { a: 1 }),
  )
  await withFetch(
    () => Promise.resolve(new Response("plain", { status: 200 })),
    async () =>
      assert.deepStrictEqual(await request("GET", "a", { token: null, baseUrl: base }), "plain"),
  )
})

test("errors: JSON message, text, empty body fallback", async () => {
  const cases: [Response, string][] = [
    [json({ message: "Nimi varattu" }, 400), "Nimi varattu"],
    [new Response("Unauthorized", { status: 401 }), "Unauthorized"],
    [new Response(null, { status: 403 }), "Pyyntö epäonnistui"],
  ]
  for (const [response, message] of cases) {
    await withFetch(
      () => Promise.resolve(response),
      async () => {
        const e = await rejection(() => request("GET", "a", { token: null, baseUrl: base }))
        assert.deepStrictEqual(e.message, message)
        assert.deepStrictEqual(e.kind, "http")
        assert.deepStrictEqual(e.status, response.status)
      },
    )
  }
})

test("network failure becomes kind network", async () => {
  await withFetch(
    () => Promise.reject(new TypeError("Failed to fetch")),
    async () => {
      const e = await rejection(() => request("GET", "a", { token: null, baseUrl: base }))
      assert.deepStrictEqual(e.kind, "network")
      assert.deepStrictEqual(e.status, 0)
    },
  )
})

test("abort rejects with the original AbortError", async () => {
  const abort = new DOMException("aborted", "AbortError")
  await withFetch(
    () => Promise.reject(abort),
    async () => {
      const e = await rejection(() => request("GET", "a", { token: null, baseUrl: base }))
      assert.equal(e, abort)
      assert(isAbortError(e))
    },
  )
})
