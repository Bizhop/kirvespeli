import { useEffect, useMemo, useState, useSyncExternalStore } from "react"
import * as api from "../api/kirves.ts"
import { useAuth } from "../auth/useAuth.ts"
import type { GameActionRequest, ReplayId } from "../types/kirves.ts"
import { createResource } from "./resource.ts"

const useResource = <T>(resource: ReturnType<typeof createResource<T>>, enabled: boolean) => {
  const state = useSyncExternalStore(resource.subscribe, resource.getState)
  useEffect(() => {
    if (!enabled) return
    resource.start()
    return resource.dispose
  }, [resource, enabled])
  return state
}

export const useGame = (id: number) => {
  const { status, sessionKey, loggingIn, gameRequest } = useAuth()
  const enabled = status === "authenticated" && !loggingIn && Number.isSafeInteger(id) && id >= 0
  const resource = useMemo(
    () => createResource(signal => gameRequest(inner => api.getGame(id, inner), signal)),
    [id, status, sessionKey, gameRequest],
  )
  const state = useResource(resource, enabled)
  // Reveal belongs to the exact confirmed snapshot, never to a later one.
  const [revealed, setRevealed] = useState<object | null>(null)
  const callbacks = useMemo(
    () => ({
      refresh: () => resource.refresh(),
      resync: () => resource.refresh(true),
      join: () => resource.mutate(signal => gameRequest(inner => api.joinGame(id, inner), signal)),
      action: ({ gameId: _gameId, ...action }: GameActionRequest) =>
        resource.mutate(signal => gameRequest(inner => api.sendAction(id, action, inner), signal)),
    }),
    [resource, id, gameRequest],
  )
  return {
    ...state,
    ...callbacks,
    authStatus: status,
    sessionKey,
    enabled,
    game: state.value,
    cardsVisible: revealed === state.value && state.value !== null,
    showAllCards: () => setRevealed(state.value),
  }
}

type LogId = Pick<ReplayId, "gameId" | "handId">

/** A lobby mount owns its list and independent log/replay resources. */
export const useGames = () => {
  const { status, sessionKey, loggingIn, user, gameRequest } = useAuth()
  const enabled = status === "authenticated" && !loggingIn
  const [selection, setSelection] = useState<{
    owner: object
    log: LogId
    replay: ReplayId | null
  } | null>(null)
  const list = useMemo(
    () => createResource(signal => gameRequest(api.listGames, signal)),
    [status, sessionKey, gameRequest],
  )
  const selected = selection?.owner === list ? selection : null
  const logId = selected?.log
  const replayId = selected?.replay
  const log = useMemo(
    () =>
      createResource(signal =>
        gameRequest(inner => api.getLog(logId!.gameId, logId!.handId, inner), signal),
      ),
    [list, logId, gameRequest],
  )
  const replay = useMemo(
    () => createResource(signal => gameRequest(inner => api.getReplay(replayId!, inner), signal)),
    [list, replayId, gameRequest],
  )
  const listState = useResource(list, enabled)
  const logState = useResource(log, enabled && !!logId)
  const replayState = useResource(replay, enabled && !!replayId)
  const [notice, setNotice] = useState<{ owner: object; message: string } | null>(null)
  const callbacks = useMemo(
    () => ({
      getGames: () => list.refresh(),
      init: () => list.mutate(signal => gameRequest(api.createGame, signal)),
      deleteGame: (id: number) =>
        list.mutate(signal => gameRequest(inner => api.deleteGame(id, inner), signal)),
      fetchGame: (id: number, signal?: AbortSignal) =>
        gameRequest(inner => api.getGame(id, inner), signal),
      getLog: (id: LogId) => {
        setNotice(null)
        setSelection({ owner: list, log: id, replay: null })
      },
      getReplay: (id: ReplayId) =>
        setSelection(current => (current?.owner === list ? { ...current, replay: id } : current)),
      restoreGame: async (id: ReplayId) => {
        setNotice(null)
        const success = await list.mutate(signal =>
          gameRequest(inner => api.restoreGame(id, inner), signal),
        )
        if (success) {
          setSelection(current => (current?.owner === list ? null : current))
          setNotice({ owner: list, message: "Pelitilanne palautettu" })
        }
      },
    }),
    [list, gameRequest],
  )
  return {
    ...callbacks,
    user,
    authStatus: status,
    sessionKey,
    enabled,
    games: listState.value ?? [],
    loading: listState.loading || logState.loading || replayState.loading,
    pending: listState.pending,
    error: listState.error || logState.error || replayState.error,
    operationError: listState.operationError,
    notice: notice?.owner === list ? notice.message : null,
    logId,
    logItems: logState.value ?? [],
    logVisible: !!logId && !!logState.value,
    replay: replayState.value,
    selectedLogIndex: replayState.value ? replayId?.index : null,
    retry: () => {
      list.refresh()
      if (logId) log.refresh()
      if (replayId) replay.refresh()
    },
  }
}
