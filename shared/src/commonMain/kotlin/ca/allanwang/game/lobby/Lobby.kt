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

interface ILobbyPlayer {
  val id: PlayerId
  val name: PlayerName
  val active: Boolean
}

@Immutable
@Serializable
data class LobbyPlayer(
 override val id: PlayerId,
 override   val name: PlayerName,
 override  val active: Boolean,
): ILobbyPlayer

@Immutable
@Serializable
sealed interface LobbyClient {

  data object NotJoined : LobbyClient

  data class Joined(val code: LobbyCode, val self: PlayerId, val players: List<LobbyPlayer>): LobbyClient

}