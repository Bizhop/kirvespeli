import type { ActionLogItem, GameAction, GameBrief, GameOut, ReplayId } from "../types/kirves.ts"
import { request } from "./http.ts"

export const createGame = (signal?: AbortSignal): Promise<GameBrief[]> =>
  request<GameBrief[]>("POST", "kirves", { signal })

export const listGames = (signal?: AbortSignal): Promise<GameBrief[]> =>
  request<GameBrief[]>("GET", "kirves", { signal })

export const joinGame = (id: number, signal?: AbortSignal): Promise<void> =>
  request("POST", `kirves/${id}`, { signal })

export const getGame = (id: number, signal?: AbortSignal): Promise<GameOut> =>
  request<GameOut>("GET", `kirves/${id}`, { signal })

export const deleteGame = (id: number, signal?: AbortSignal): Promise<void> =>
  request("DELETE", `kirves/${id}`, { signal })

export const sendAction = (id: number, action: GameAction, signal?: AbortSignal): Promise<void> =>
  request("PUT", `kirves/${id}`, { body: action, signal })

export const getLog = (
  id: number,
  handId: number,
  signal?: AbortSignal,
): Promise<ActionLogItem[]> => request<ActionLogItem[]>("GET", `kirves/${id}/${handId}`, { signal })

export const getReplay = (r: ReplayId, signal?: AbortSignal): Promise<GameOut> =>
  request<GameOut>("GET", `kirves/${r.gameId}/${r.handId}/${r.index}`, { signal })

export const restoreGame = (r: ReplayId, signal?: AbortSignal): Promise<void> =>
  request("POST", `kirves/${r.gameId}/${r.handId}/${r.index}`, { signal })
