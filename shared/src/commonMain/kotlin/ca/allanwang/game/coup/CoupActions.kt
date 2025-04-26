package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import ca.allanwang.game.coup.Card.Ambassador
import ca.allanwang.game.coup.Card.Assassin
import ca.allanwang.game.coup.Card.Captain
import ca.allanwang.game.coup.Card.Contessa
import ca.allanwang.game.coup.Card.Duke
import ca.allanwang.game.lobby.PlayerId
import kotlinx.serialization.Serializable

enum class CoupCardAction(
  val actingCard: Card?, val targetsPlayer: Boolean, val blockingCards: List<Card>,
) {
  Income(actingCard = null, targetsPlayer = false, blockingCards = emptyList()),
  ForeignAid(
    actingCard = null,
    targetsPlayer = false,
    blockingCards = listOf(Duke)
  ),
  Tax(actingCard = Duke, targetsPlayer = false, blockingCards = emptyList()),
  Assassinate(
    actingCard = Assassin,
    targetsPlayer = true,
    blockingCards = listOf(Contessa)
  ),
  Coup(actingCard = null, targetsPlayer = true, blockingCards = emptyList()),
  Steal(
    actingCard = Captain,
    targetsPlayer = true,
    blockingCards = listOf(Ambassador, Captain)
  ),
  Exchange(actingCard = Ambassador, targetsPlayer = false, blockingCards = emptyList())
}

@Immutable
@Serializable
sealed interface CoupAction

@Serializable
data class SelectAction(val action: CoupCardAction) : CoupAction

@Serializable
data class SelectPlayer(val player: PlayerId) : CoupAction

@Serializable
data object ContestAction : CoupAction

@Serializable
data class BlockAction(val card: Card) : CoupAction

@Serializable
data object AcceptAction : CoupAction

@Serializable
data class LoseCard(val card: Card): CoupAction

@Serializable
data class SelectCards(val cards: List<Card>) : CoupAction
