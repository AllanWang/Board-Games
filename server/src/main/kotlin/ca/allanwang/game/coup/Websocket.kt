package ca.allanwang.game.coup

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText

fun Route.coupSockets() {
  webSocket {
    outgoing.send(Frame.Text("Hello"))
    for (frame in incoming) {
      if (frame is Frame.Text) {
        val text = frame.readText()
        outgoing.send(Frame.Text("YOU SAID: $text"))
        if (text.equals("bye", ignoreCase = true)) {
          close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
        }
      }
    }
  }
}