package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import ca.allanwang.game.lobby.Lobby
import ca.allanwang.game.lobby.LobbyPlayer
import ca.allanwang.game.lobby.PlayerId
import kotlinx.serialization.Serializable

enum class PlayerAction {
  Income, ForeignAid, Tax, Assassinate, Coup, Steal, Exchange
}

@Immutable
@Serializable
sealed interface CoupMessage

sealed interface CoupMessageClient: CoupMessage

data class SelectedAction(val action: PlayerAction) : CoupMessageClient

data class ContestAction(val card: Card) : CoupMessageClient

data object AcceptAction : CoupMessageClient

sealed interface CoupMessageServer: CoupMessage

data class LobbyInfo(val self: PlayerId, val lobby: Lobby) : CoupMessageServer

data object SelectAction : CoupMessageServer

data class SelectPlayerForAction(
  val action: PlayerAction,
  val options: List<PlayerId>,
  val coupRequired: Boolean = false,
) :
  CoupMessageServer

data class CheckContest(val action: PlayerAction, val requester: PlayerId, val recipient: PlayerId?) : CoupMessageServer

data class SelectCard(val options: List<Card>, val count: Int) : CoupMessageServer

data class LoseCard(val id: PlayerId) : CoupMessageServer

data class WaitingFor(val id: PlayerId) : CoupMessageServer

data object Lost : CoupMessageServer

data class GameEnd(val winner: PlayerId) : CoupMessageServer
