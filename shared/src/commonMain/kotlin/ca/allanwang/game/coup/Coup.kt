package ca.allanwang.game.coup

import androidx.compose.runtime.Immutable
import ca.allanwang.game.coup.Coup.CoupPlayerAction
import ca.allanwang.game.lobby.ILobbyPlayer
import ca.allanwang.game.lobby.LobbyCode
import ca.allanwang.game.lobby.LobbyPlayer
import ca.allanwang.game.lobby.PlayerId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Immutable
@Serializable
data class Coup(
  val active: Int,
  val code: LobbyCode,
  val players: List<Player>,
  val deck: List<Card>,
  val discard: List<Card>,
  val playerAction: CoupPlayerAction,
) {

  @Serializable
  data class PendingAction(
    val action: CoupCardAction,
    val requester: PlayerId,
    val recipient: PlayerId?
  )

  @Serializable
  data class PendingBlockAction(
    val blockingPlayer: PlayerId,
    val blockingCard: Card,
    val action: PendingAction,
  )

  @Immutable
  @Serializable
  sealed interface CoupPlayerAction {
    @Serializable
    data object Waiting : CoupPlayerAction

    @Serializable
    data class GameEnded(val winner: PlayerId): CoupPlayerAction

    @Serializable
    data class SelectingAction(val options: List<CoupCardAction>) : CoupPlayerAction

    @Serializable
    data class SelectingPlayer(val action: CoupCardAction, val options: List<PlayerId>, val requireCoup: Boolean) :
      CoupPlayerAction

    @Serializable
    data class Validating(
      val pendingAction: PendingAction, val accepted: Set<PlayerId>,
    ) : CoupPlayerAction

    @Serializable
    data class ValidatingBlock(
     val pendingBlock: PendingBlockAction,
    ) : CoupPlayerAction

    @Serializable
    data class RequestingLoseCard(val playerId: PlayerId, val reason: Reason) : CoupPlayerAction {
      @Immutable
      @Serializable
      sealed interface Reason

      @Serializable
      data class Assassinated(val requester: PlayerId) : Reason
      @Serializable
      data class Couped(val requester: PlayerId) : Reason
      @Serializable
      data class ContestFailed(val pendingAction: PendingAction, ) : Reason
      @Serializable
      data class ContestSucceeded(val pendingAction: PendingAction, ) :
        Reason
      @Serializable
      data class BlockContestFailed(val pendingBlock: PendingBlockAction, ) : Reason
      @Serializable
      data class BlockContestSucceeded(val pendingBlock: PendingBlockAction,) :
        Reason
    }

    @Serializable
    data class SelectingCard(val playerId: PlayerId, val cards: List<Card>, val count: Int) : CoupPlayerAction
  }
}

@Immutable
@Serializable
data class Player(
  val lobbyPlayer: LobbyPlayer,
  val gameInfo: GamePlayerInfo,
) : ILobbyPlayer by lobbyPlayer

@Immutable
@Serializable
data class GamePlayerInfo(
  val coins: Int,
  val cards: List<Card>,
)

@Immutable
@Serializable
enum class Card {
  Ambassador, Assassin, Captain, Contessa, Duke,
}

@Immutable
@Serializable
data class CoupClient(
  val self: PlayerId,
  val active: Int,
  val players: List<LobbyPlayer>,
  val gameInfo: GameInfo,
  val action: CoupPlayerAction,
) {

  @Immutable
  @Serializable
  sealed interface GameInfo {

    @Serializable
    @JvmInline
    value class PlayerInfo(val info: GamePlayerInfo) : GameInfo

    @Serializable
    data object Lost : GameInfo

    @Serializable
    data class GameEnded(val winner: PlayerId) : GameInfo
  }
}
