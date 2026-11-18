import { test } from "node:test"
import assert from "node:assert/strict"
import { createResource } from "../src/game/resource.ts"

const deferred = <T>() => {
  let resolve!: (value: T) => void
  let reject!: (error: unknown) => void
  const promise = new Promise<T>((res, rej) => {
    resolve = res
    reject = rej
  })
  return { promise, resolve, reject }
}
const tick = () => new Promise(resolve => setTimeout(resolve, 0))
const setup = () => {
  const requests: ReturnType<typeof deferred<string>>[] = []
  const signals: AbortSignal[] = []
  const resource = createResource(signal => {
    const request = deferred<string>()
    requests.push(request)
    signals.push(signal)
    return request.promise // Deliberately ignores abort to exercise late-response guards.
  })
  return { resource, requests, signals }
}

test("notifications during a fetch coalesce into a follow-up without dropping confirmed snapshots", async () => {
  const { resource, requests } = setup()
  resource.start()
  resource.refresh()
  resource.refresh()
  assert.equal(requests.length, 1)
  assert.equal(resource.getState().value, null)
  requests[0].resolve("first")
  await tick()
  assert.equal(resource.getState().value, "first")
  assert.equal(requests.length, 2)
  resource.refresh()
  requests[1].resolve("second")
  await tick()
  assert.equal(resource.getState().value, "second")
  assert.equal(requests.length, 3)
  requests[2].resolve("latest")
  await tick()
  assert.equal(resource.getState().value, "latest")
  assert.equal(resource.getState().loading, false)
  resource.dispose()
})

test("successful mutation refetches without a notification and blocks duplicates until confirmation", async () => {
  const { resource, requests } = setup()
  resource.start()
  requests[0].resolve("before")
  await tick()
  const write = deferred<void>()
  const mutation = resource.mutate(() => write.promise)
  let duplicateCalls = 0
  assert.equal(
    await resource.mutate(async () => {
      duplicateCalls++
    }),
    false,
  )
  write.resolve()
  assert.equal(await mutation, true)
  assert.equal(resource.getState().pending, true)
  assert.equal(requests.length, 2)
  assert.equal(
    await resource.mutate(async () => {
      duplicateCalls++
    }),
    false,
  )
  assert.equal(duplicateCalls, 0)
  requests[1].resolve("confirmed")
  await tick()
  assert.equal(resource.getState().value, "confirmed")
  assert.equal(resource.getState().pending, false)
  resource.dispose()
})

test("join rejection is retained through refetch and a failed refresh remains retryable", async () => {
  const { resource, requests } = setup()
  resource.start()
  requests[0].resolve("before")
  await tick()
  assert.equal(
    await resource.mutate(async () => {
      throw new Error("Peli on täynnä")
    }),
    false,
  )
  assert.equal(resource.getState().operationError, "Peli on täynnä")
  requests[1].reject(new Error("offline"))
  await tick()
  assert.equal(resource.getState().pending, false)
  assert.equal(resource.getState().value, "before")
  assert.equal(resource.getState().error, "offline")
  resource.refresh()
  requests[2].resolve("full game")
  await tick()
  assert.equal(resource.getState().error, null)
  assert.equal(resource.getState().operationError, "Peli on täynnä")
  assert.equal(resource.getState().value, "full game")
  resource.dispose()
})

test("mutation supersedes an earlier fetch, including notifications while writing", async () => {
  const { resource, requests, signals } = setup()
  resource.start()
  const write = deferred<void>()
  const mutation = resource.mutate(() => write.promise)
  assert.equal(signals[0].aborted, true)
  resource.refresh()
  requests[0].resolve("stale before action")
  await tick()
  assert.equal(resource.getState().value, null)
  assert.equal(requests.length, 1)
  write.resolve()
  await mutation
  assert.equal(requests.length, 2)
  requests[1].resolve("after action")
  await tick()
  assert.equal(resource.getState().value, "after action")
  assert.equal(resource.getState().pending, false)
  resource.dispose()
})

test("reconnect discards a preconnection read and resets animations with the new snapshot", async () => {
  const { resource, requests } = setup()
  resource.start()
  requests[0].resolve("before disconnect")
  await tick()
  resource.refresh()
  resource.refresh(true)
  assert.equal(resource.getState().resetKey, 0)
  requests[1].resolve("stale")
  await tick()
  assert.equal(resource.getState().value, "before disconnect")
  requests[2].resolve("resynced")
  await tick()
  assert.equal(resource.getState().value, "resynced")
  assert.equal(resource.getState().resetKey, 1)
  resource.refresh()
  requests[3].resolve("next live change")
  await tick()
  assert.equal(resource.getState().resetKey, 1)
  resource.dispose()
})

test("route/replay selection cleanup prevents old data and errors appearing in the new selection", async () => {
  const old = setup()
  const next = setup()
  old.resource.start()
  old.resource.dispose()
  next.resource.start()
  next.requests[0].resolve("new selection")
  old.requests[0].reject(new Error("old selection error"))
  await tick()
  assert.equal(old.signals[0].aborted, true)
  assert.equal(old.resource.getState().value, null)
  assert.equal(old.resource.getState().error, null)
  assert.equal(next.resource.getState().value, "new selection")
  next.resource.dispose()
})

test("cleanup during mutation cancels it, never refetches and permits a strict-mode restart", async () => {
  const { resource, requests } = setup()
  resource.start()
  let signal!: AbortSignal
  const write = deferred<void>()
  const mutation = resource.mutate(s => {
    signal = s
    return write.promise
  })
  resource.dispose()
  assert.equal(signal.aborted, true)
  resource.start()
  requests[1].resolve("new mount")
  requests[0].resolve("old mount")
  write.resolve()
  assert.equal(await mutation, false)
  await tick()
  assert.equal(resource.getState().value, "new mount")
  assert.equal(resource.getState().pending, false)
  assert.equal(requests.length, 2)
  resource.dispose()
})

test("reconnect during mutation still reconciles and releases pending state", async () => {
  const { resource, requests } = setup()
  resource.start()
  requests[0].resolve("before")
  await tick()
  const write = deferred<void>()
  const mutation = resource.mutate(() => write.promise)
  resource.refresh(true)
  write.resolve()
  assert.equal(await mutation, true)
  requests[1].resolve("after reconnect and action")
  await tick()
  assert.equal(resource.getState().value, "after reconnect and action")
  assert.equal(resource.getState().pending, false)
  assert.equal(resource.getState().resetKey, 1)
  resource.dispose()
})
