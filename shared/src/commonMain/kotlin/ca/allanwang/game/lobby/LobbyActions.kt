package ca.allanwang.game.lobby

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface LobbyAction

data class Join(val name: PlayerName) : LobbyAction

data object Start : LobbyAction

@Immutable
@Serializable
sealed interface LobbyActionClient

data class JoinFailure(val reason: JoinFailure) : LobbyActionClient {
  sealed interface JoinFailure

  data class DuplicateName(val name: PlayerName) : JoinFailure
}


