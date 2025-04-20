package ca.allanwang.game.lobby

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface LobbyMessage

sealed interface LobbyMessageClient: LobbyMessage

data class Join(val name: PlayerName) : LobbyMessageClient

data object Start : LobbyMessageClient

sealed interface LobbyMessageServer: LobbyMessage

data class JoinFailure(val reason: JoinFailure) : LobbyMessageServer {
  sealed interface JoinFailure

  data class DuplicateName(val name: PlayerName) : JoinFailure
}

data class LobbyUpdate(val lobby: Lobby) : LobbyMessageServer

data object StartGame : LobbyMessageServer

