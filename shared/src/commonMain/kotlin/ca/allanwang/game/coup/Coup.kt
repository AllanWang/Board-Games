package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@JvmInline
value class PlayerId(val id: String)

@Immutable
data class Coup(
  val players: List<Player>,
  val logs: List<String>
)

@Immutable
data class PlayerState(
  val id: PlayerId,
  val name: String,
  val others: List<String>,
  val coins: Int,
  val cards: List<Card>,

)

@Immutable
data class Player(
  val id: PlayerId,
  val name: String,
  val coins: Int,
  val cards: List<Card>,
)

enum class Card {
  Ambassador, Assassin, Captain, Contessa, Duke,
}

@Immutable
data class Lobby(
  val code: String,
  val players: List<LobbyPlayer>,
)

@Immutable
data class LobbyPlayer(
  val id: PlayerId,
  val name: String,
  val ready: Boolean,
)