package ca.allanwang.game

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.allanwang.game.coup.PlayerState
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

@Immutable
class State(private val scope: CoroutineScope) {
  var logs: List<String> by mutableStateOf(listOf("Logs Here"))
    private set

  @OptIn(ExperimentalSerializationApi::class)
  private val client: HttpClient =
    HttpClient(Js) {
      install(ContentNegotiation) {
        protobuf(ProtoBuf {
          encodeDefaults = true
        })
      }
      install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
      }
    }

  fun addLog(text: String) {
    logs += text
  }

  private val _flow: MutableStateFlow<PlayerState?> = MutableStateFlow(null)
  val flow: StateFlow<PlayerState?> get() = _flow

  suspend fun connect() {
    client.webSocket(
      method = HttpMethod.Get,
      host = "localhost",
      port = SERVER_PORT,
      path = "/ws/coup"
    ) {
      while (true) {
        try {
          val playerState = receiveDeserialized<PlayerState>()
          addLog(playerState.toString())
        } catch (e: WebsocketDeserializeException) {
          addLog("Failed to deserialize")
        }
      }
    }
  }

  suspend fun send(text: String) {

  }
}