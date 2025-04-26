package ca.allanwang.game.lobby

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface LobbyAction

@Serializable
data class Join(val id: PlayerId) : LobbyAction

@Serializable
data object Start : LobbyAction
