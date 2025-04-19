package ca.allanwang.game

import ca.allanwang.game.coup.coupSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSockets() {
  install(WebSockets) {
    pingPeriod = 15.seconds
    timeout = 15.seconds
    maxFrameSize = Long.MAX_VALUE
    masking = false
    contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
  }
  routing {
    route("ws") {
      route("coup") {
        coupSockets()
      }
      route("test") {
        webSocket {
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
    }
  }
}