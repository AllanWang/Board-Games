package ca.allanwang.game

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.allanwang.game.coup.Coup.CoupPlayerAction
import ca.allanwang.game.coup.CoupAction
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow.DROP_LATEST
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
        protobuf(ProtobufSerializer)
      }
      install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(ProtobufSerializer)
      }
    }

  fun addLog(text: String) {
    logs += text
  }

  private val _flow: MutableStateFlow<GameClient> = MutableStateFlow(GameClientEmpty)
  val flow: StateFlow<GameClient> get() = _flow

  private val sendFlow: MutableSharedFlow<CoupPlayerAction> =
    MutableSharedFlow(replay = 0, extraBufferCapacity = 5, onBufferOverflow = DROP_OLDEST)

  suspend fun connect() {
    client.webSocket(
      method = HttpMethod.Get,
      host = "localhost",
      port = SERVER_PORT,
      path = "/ws/game"
    ) {
      addLog("Connect")
      val job = Job()
      launch(job) {
        while (isActive) {
          try {
            val gameClient = receiveDeserialized<GameClient>()
            _flow.emit(gameClient)
          } catch (e: ClosedReceiveChannelException) {
            addLog("Channel closed")
            job.cancel()
            break
          } catch (e: WebsocketDeserializeException) {
            addLog("Failed to deserialize ${e.message}")
          } catch (e: Exception) {
            e.printStackTrace()
            addLog(e.message ?: "error")
          }
        }
      }
      launch(job) {
        sendFlow.collect {
          action: CoupPlayerAction ->
          sendSerialized(action, typeInfo<CoupPlayerAction>())
        }
      }
      job.join()
      addLog("Socket closed")
    }
  }

  fun send(action: CoupPlayerAction) {
    sendFlow.tryEmit(action)
  }
}