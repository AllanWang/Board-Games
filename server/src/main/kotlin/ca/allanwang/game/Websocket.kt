package ca.allanwang.game

import ca.allanwang.game.lobby.Join
import ca.allanwang.game.lobby.LobbyAction
import ca.allanwang.game.lobby.PlayerId
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSockets() {
  install(WebSockets) {
    pingPeriod = 15.seconds
    timeout = 15.seconds
    maxFrameSize = Long.MAX_VALUE
    masking = false
    contentConverter = KotlinxWebsocketSerializationConverter(ProtobufSerializer)
  }
  val game = GameStore()
  routing {
    route("ws") {
      webSocket("game") {
        log.info("Started websocket")
        val job = Job()

        var playerId  = PlayerId("__uninitialized__")

        // Get valid id first
        while (isActive) {
          val action = receiveDeserialized<GameAction>()
          val newPlayerId = ((action as? GameActionLobby)?.action as? Join)?.id
          if (newPlayerId != null) {
            log.trace("Received id $newPlayerId")
            playerId = newPlayerId
            game.dispatch(playerId, action)
            break
          }
        }

        launch(job) {
          while (isActive) {
            try {
              val action = receiveDeserialized<GameAction>()
              log.trace("Received for {}: {}", playerId, action)
              game.dispatch(playerId, action)
            } catch (e: ClosedReceiveChannelException) {
              job.cancel()
              break
            } catch (e: WebsocketDeserializeException) {
              log.error(e)
            }
          }
        }
        launch(job) {
          game.observe(playerId).collect { state ->
            log.trace("Sending for {}: {}", playerId, state)
            // Added explicit type info for serialization
            sendSerialized(state, typeInfo<GameClient>())
          }
        }
        job.join()
        log.info("Socket closed for {}", playerId)
      }
      webSocket("test") {
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