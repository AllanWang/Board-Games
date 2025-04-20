package ca.allanwang.game.lobby

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class PlayerId(val id: String)

@JvmInline
@Serializable
value class PlayerName(val name: String)

@JvmInline
@Serializable
value class LobbyCode(val code: String)

@Immutable
@Serializable
data class Lobby(val code: LobbyCode, val players: List<LobbyPlayer>)

@Immutable
@Serializable
data class LobbyPlayer(
  val id: PlayerId,
  val name: PlayerName,
)

@Immutable
@Serializable
sealed interface LobbyClient {

  data object NotJoined : LobbyClient

  data class NotJoinedBadName(val name: PlayerName): LobbyClient

  data class Joined(val code: LobbyCode, val self: PlayerId, val players: List<LobbyPlayer>): LobbyClient

}