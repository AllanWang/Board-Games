package ca.allanwang.game.coup

import io.ktor.server.routing.Route
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText

fun Route.coupSockets() {
  webSocket {
    sendSerialized(
      PlayerState(
        id = PlayerId("test"),
        name = "Test",
        coins = 2,
        others = emptyList(),
        cards = emptyList()
      )
    )
    for (frame in incoming) {
      if (frame is Frame.Text) {
        val text = frame.readText()
        println("Received $text")
        if (text.equals("bye", ignoreCase = true)) {
          close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
        }
      }
    }
  }
}