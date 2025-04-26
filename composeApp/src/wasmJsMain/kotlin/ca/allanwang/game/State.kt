package ca.allanwang.game

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.allanwang.game.coup.CoupAction
import ca.allanwang.game.lobby.Join
import ca.allanwang.game.lobby.LobbyAction
import ca.allanwang.game.lobby.PlayerId
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Immutable
class State(private val scope: CoroutineScope) {

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

  var state: GameClient by mutableStateOf(GameClientEmpty)
    private set

  private val sendFlow: MutableSharedFlow<GameAction> =
    MutableSharedFlow(replay = 0, extraBufferCapacity = 5, onBufferOverflow = DROP_OLDEST)

  private val websocketJob = SupervisorJob()

  private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    println(throwable.message)
  }

  fun connect(id: PlayerId) {
    websocketJob.cancelChildren()
    scope.launch(websocketJob) {
      client.webSocket(
        method = HttpMethod.Get,
        host = "localhost",
        port = SERVER_PORT,
        path = "/ws/game"
      ) {
        println("Connect")
        val job = Job(parent = websocketJob)
        launch(job + exceptionHandler) {
          while (isActive) {
            try {
              val gameClient = receiveDeserialized<GameClient>()
              state = gameClient
            } catch (e: ClosedReceiveChannelException) {
              println("Channel closed")
              job.cancel()
              break
            } catch (e: WebsocketDeserializeException) {
              println("Failed to deserialize ${e.message}")
            } catch (e: Exception) {
              e.printStackTrace()
              println(e.message ?: "error")
            }
          }
        }
        launch(job + exceptionHandler) {
          sendSerialized(GameActionLobby(LobbyAction.Join(id = id)), typeInfo<GameAction>())
          sendFlow.collect { action: GameAction ->
            sendSerialized(action, typeInfo<GameAction>())
          }
        }
        job.join()
        println("Socket closed")
      }
    }
  }

  fun send(action: GameAction) {
    println("Send $action")
    if (!sendFlow.tryEmit(action)) {
      println("Failed to emit")
    }
  }

  fun send(action: LobbyAction) = send(GameActionLobby(action = action))

  fun send(action: CoupAction) = send(GameActionCoup(action = action))
}