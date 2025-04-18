package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@JvmInline
value class PlayerId(val id: String)

@Immutable
data class Coup(
  val players: List<Player>
)

data class Player(
  val id: PlayerId,
  val name: String,
  val coins: Int,
  val cards: List<Card>,
)

enum class Card {
  Ambassador, Assassin, Captain, Contessa, Duke,
}
