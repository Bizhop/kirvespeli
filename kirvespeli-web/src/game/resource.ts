import { isAbortError } from "../api/http.ts"

export interface ResourceState<T> {
  value: T | null
  loading: boolean
  error: string | null
  pending: boolean
  operationError: string | null
  resetKey: number
}

/** One page-owned resource. Invalidations coalesce, but never disappear during a fetch.
 * Each completed fetch can commit before the next starts, preserving animation snapshots.
 * Mutations supersede in-flight reads and always reconcile with a fetch, even on failure.
 */
export const createResource = <T>(load: (signal: AbortSignal) => Promise<T>) => {
  let state: ResourceState<T> = {
    value: null,
    loading: false,
    error: null,
    pending: false,
    operationError: null,
    resetKey: 0,
  }
  const listeners = new Set<() => void>()
  let active = false
  let generation = 0
  let dirty = false
  let writing = false
  let resetPending = false
  let reading = false
  let readController: AbortController | null = null
  let writeController: AbortController | null = null
  const patch = (next: Partial<ResourceState<T>>) => {
    state = { ...state, ...next }
    listeners.forEach(listener => listener())
  }
  const message = (e: unknown) => (e instanceof Error ? e.message : "Pyyntö epäonnistui")
  const pump = async () => {
    if (!active || reading || writing || !dirty) return
    const started = generation
    reading = true
    dirty = false
    const controller = new AbortController()
    readController = controller
    patch({ loading: true, error: null })
    try {
      const value = await load(controller.signal)
      if (active && generation === started && !controller.signal.aborted) {
        patch({
          value,
          resetKey: state.resetKey + (resetPending ? 1 : 0),
        })
        resetPending = false
      }
    } catch (e) {
      if (active && generation === started && !controller.signal.aborted && !isAbortError(e)) {
        patch({ error: message(e) })
      }
    } finally {
      // A disposed/restarted resource owns a different request now.
      if (readController === controller) {
        reading = false
        readController = null
        if (active) {
          patch({ loading: false, pending: state.pending && (writing || dirty) })
          void pump()
        }
      }
    }
  }
  const refresh = (reset = false) => {
    if (!active) return
    if (reset) {
      resetPending = true
      readController?.abort()
    }
    dirty = true
    void pump()
  }
  const mutate = async (operation: (signal: AbortSignal) => Promise<unknown>): Promise<boolean> => {
    if (!active || state.pending) return false
    generation += 1
    writing = true
    // Keep one read at a time even if the transport ignores cancellation.
    readController?.abort()
    const started = generation
    const controller = new AbortController()
    writeController = controller
    patch({ pending: true, operationError: null })
    let success = false
    try {
      await operation(controller.signal)
      success = active && started === generation && !controller.signal.aborted
    } catch (e) {
      if (active && started === generation && !controller.signal.aborted && !isAbortError(e)) {
        patch({ operationError: message(e) })
      }
    } finally {
      if (active && started === generation) {
        writeController = null
        writing = false
        refresh()
      }
    }
    return success
  }
  return {
    getState: () => state,
    subscribe: (listener: () => void) => {
      listeners.add(listener)
      return () => {
        listeners.delete(listener)
      }
    },
    start: () => {
      if (active) return
      active = true
      refresh()
    },
    dispose: () => {
      active = false
      generation += 1
      dirty = false
      writing = false
      resetPending = false
      readController?.abort()
      writeController?.abort()
      readController = null
      writeController = null
      reading = false
      patch({ value: null, loading: false, pending: false, error: null, operationError: null })
    },
    refresh,
    mutate,
  }
}
