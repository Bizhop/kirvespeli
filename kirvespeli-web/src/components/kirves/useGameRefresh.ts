import { useEffect, useRef } from "react"
import { Client } from "@stomp/stompjs"

// Calls onRefresh(gameId) when the backend announces a change to this game on /topic/refresh.
// On connection, notifications may have been missed: onResync(gameId) is called so
// the caller can fetch a fresh snapshot and present it without transitions.
const useGameRefresh = (
  gameId: number | null,
  onRefresh: (id: number) => void,
  enabled = true,
  onResync?: (id: number) => void,
) => {
  const resync = useRef(onResync)
  resync.current = onResync
  useEffect(() => {
    if (!enabled || gameId == null) {
      return
    }
    let active = true
    const client = new Client({
      brokerURL: import.meta.env.VITE_WEB_SOCKET_URL,
      reconnectDelay: 5000,
      onConnect: () => {
        if (!active) return
        client.subscribe("/topic/refresh", message => {
          if (active && message.body === String(gameId)) {
            onRefresh(gameId)
          }
        })
        // Subscribe first, then fetch: also closes the initial fetch/subscription gap.
        resync.current?.(gameId)
      },
    })
    client.activate()
    return () => {
      active = false
      void client.deactivate()
    }
  }, [gameId, onRefresh, enabled])
}

export default useGameRefresh
