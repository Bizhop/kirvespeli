import { useEffect, useRef } from "react"
import { Client } from "@stomp/stompjs"

// Calls onRefresh(gameId) when the backend announces a change to this game on /topic/refresh.
// After a reconnect, notifications may have been missed: onResync(gameId) is called instead so
// the caller can fetch a fresh snapshot and present it without transitions.
const useGameRefresh = (gameId, onRefresh, enabled = true, onResync) => {
  const resync = useRef(onResync)
  resync.current = onResync
  useEffect(() => {
    if (!enabled || gameId == null) {
      return
    }
    let connected = false
    const client = new Client({
      brokerURL: process.env.WEB_SOCKET_URL,
      reconnectDelay: 5000,
      onConnect: () => {
        if (connected) resync.current?.(gameId)
        connected = true
        client.subscribe("/topic/refresh", message => {
          if (message.body === String(gameId)) {
            onRefresh(gameId)
          }
        })
      },
    })
    client.activate()
    return () => {
      client.deactivate()
    }
  }, [gameId, onRefresh, enabled])
}

export default useGameRefresh
