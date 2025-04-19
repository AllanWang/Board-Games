package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class PlayerId(val id: String)

@Immutable
@Serializable
data class Coup(
  val players: List<Player>,
  val logs: List<String>
)

@Immutable
@Serializable
data class PlayerState(
  val id: PlayerId,
  val name: String,
  val others: List<String>,
  val coins: Int,
  val cards: List<Card>,

)

@Immutable
@Serializable
data class Player(
  val id: PlayerId,
  val name: String,
  val coins: Int,
  val cards: List<Card>,
)

@Serializable
enum class Card {
  Ambassador, Assassin, Captain, Contessa, Duke,
}

@Immutable
@Serializable
data class Lobby(
  val code: String,
  val players: List<LobbyPlayer>,
)

@Immutable
@Serializable
data class LobbyPlayer(
  val id: PlayerId,
  val name: String,
  val ready: Boolean,
)