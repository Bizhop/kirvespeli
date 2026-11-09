import { useEffect } from "react"
import { Client } from "@stomp/stompjs"

// Calls onRefresh(gameId) when the backend announces a change to this game on /topic/refresh
const useGameRefresh = (gameId, onRefresh, enabled = true) => {
  useEffect(() => {
    if (!enabled || gameId == null) {
      return
    }
    const client = new Client({
      brokerURL: process.env.WEB_SOCKET_URL,
      reconnectDelay: 5000,
      onConnect: () => {
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
