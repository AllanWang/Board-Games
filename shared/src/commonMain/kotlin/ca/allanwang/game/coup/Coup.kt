package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import ca.allanwang.game.lobby.PlayerId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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