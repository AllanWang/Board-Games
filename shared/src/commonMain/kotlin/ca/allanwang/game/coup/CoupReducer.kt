package ca.allanwang.game.coup

import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.Assassinated
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.BlockContestFailed
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.ContestFailed
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.ContestSucceeded
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.Couped
import ca.allanwang.game.coup.Coup.CoupPlayerAction.Selecting
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingCard
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingPlayer
import ca.allanwang.game.coup.Coup.CoupPlayerAction.Validating
import ca.allanwang.game.coup.Coup.CoupPlayerAction.ValidatingBlock
import ca.allanwang.game.coup.Coup.CoupPlayerAction.Waiting
import ca.allanwang.game.coup.Coup.PendingAction
import ca.allanwang.game.coup.CoupCardAction.Assassinate
import ca.allanwang.game.coup.CoupCardAction.Exchange
import ca.allanwang.game.coup.CoupCardAction.ForeignAid
import ca.allanwang.game.coup.CoupCardAction.Income
import ca.allanwang.game.coup.CoupCardAction.Steal
import ca.allanwang.game.coup.CoupCardAction.Tax
import ca.allanwang.game.lobby.PlayerId
import ca.allanwang.game.redux.StoreReducer
import ca.allanwang.game.redux.thenIf

object CoupReducer : StoreReducer<Coup, CoupClient, CoupAction, CoupActionClient> {

  private fun Coup.findPlayer(playerId: PlayerId): Player? =
    players.firstOrNull { it.id == playerId }

  private val Coup.activeCount: Int
    get() = players.count { it.active }

  private fun Coup.updatePlayer(playerId: PlayerId, action: GamePlayerInfo.() -> GamePlayerInfo): Coup =
    copy(players = players.map { player ->
      if (player.id == playerId) player.updateGameInfo(action) else player
    })

  private fun Player.updateGameInfo(action: GamePlayerInfo.() -> GamePlayerInfo): Player =
    copy(gameInfo = gameInfo.action())

  private fun Coup.checkPlayerCard(playerId: PlayerId, card: Card?): Boolean {
    if (card == null) return true
    val gameInfo =  findPlayer(playerId)?.gameInfo ?: return false
    return card in gameInfo.cards
  }

  override fun reduce(
    state: Coup,
    playerId: PlayerId,
    action: CoupAction,
    dispatch: (CoupAction) -> Unit,
    clientDispatch: (CoupActionClient) -> Unit,
  ): Coup {
    val player = state.findPlayer(playerId) ?: return state
    val isCurrentActivePlayer = playerId == state.players[state.active].id
    val playerAction = state.playerAction
    if (!player.active) {
      return state
    }
    return when (action) {
      is SelectAction -> {
        // Incorrect player
        if (!isCurrentActivePlayer) {
          return state
        }
        if (playerAction !is Selecting) {
          return state
        }
        val cardAction = action.action
        if (cardAction !in playerAction.options) {
          return state
        }
        state.selectAction(requester = player, action = cardAction, recipient = null)
      }

      is SelectPlayer -> {
        // Incorrect player
        if (!isCurrentActivePlayer) {
          return state
        }
        if (playerAction !is SelectingPlayer) {
          return state
        }
        if (action.player == playerId) {
          return state
        }
        val recipient = state.findPlayer(action.player) ?: return state
        state.selectAction(requester = player, action = playerAction.action, recipient = recipient.id)
      }

      is AcceptAction -> {
        when (playerAction) {
          is Validating -> {
            val newAccepted = playerAction.accepted + player.id
            if (newAccepted.size == state.activeCount) {
              TODO("act")
            } else {
              val newPlayerAction = playerAction.copy(accepted = playerAction.accepted + player.id)
              state.copy(playerAction = newPlayerAction)
            }
          }

          is ValidatingBlock -> {
            if (isCurrentActivePlayer) {
              return state
            }
            TODO("next turn")
          }

          else -> state
        }
      }

      is ContestAction -> {
        val contesterId = playerId
        when (playerAction) {
          is Validating -> {
            val card = playerAction.pendingAction.action.actingCard
            val requesterId = playerAction.pendingAction.requester

            val newPlayerAction = if (state.checkPlayerCard(requesterId, card)) {
              // Contester loses card
              RequestingLoseCard(
                playerId = contesterId,
                reason = ContestFailed(
                  pendingAction = playerAction.pendingAction
                )
              )
            } else {
              // Contester succeeds
              // TODO incorrect, switch cards instead
              RequestingLoseCard(
                playerId = requesterId,
                reason = ContestSucceeded(
                  pendingAction = playerAction.pendingAction
                )
              )
            }
            state.copy(playerAction = newPlayerAction)
          }

          is ValidatingBlock -> {
            if (playerId != playerAction.pendingBlock.action.requester) {
              return state
            }
            if (state.checkPlayerCard(playerAction.pendingBlock.blockingPlayer, playerAction.pendingBlock.blockingCard)) {
              // Contester (self) loses card
              RequestingLoseCard(
                playerId = contesterId,
                reason = BlockContestFailed(
                  pendingBlock = playerAction.pendingBlock
                )
              )
            } else {
              // Contester succeeds
              // TODO incorrect, switch cards instead
              RequestingLoseCard(
                playerId = contesterId,
                reason = BlockContestFailed(
                  pendingBlock = playerAction.pendingBlock
                )
              )
            }
            TODO()
          }

          else -> state
        }
      }

      is BlockAction -> {

        TODO()
      }
      is SelectCards -> {
        if (playerAction !is SelectingCard) {
          return state
        }
        if (playerAction.playerId != playerId) {
          return state
        }
        if (action.cards.size != playerAction.count) {
          return state
        }
        val deckCards = playerAction.cards.toMutableList()
        for (card in action.cards) {
          if (!deckCards.remove(card)) {
            return state
          }
        }
        state.updatePlayer(playerAction.playerId) {
          copy(cards = action.cards)
        }.copy(deck = state.deck + deckCards.shuffled())
      }
    }
  }

  private fun Coup.selectAction(requester: Player, action: CoupCardAction, recipient: PlayerId?): Coup {
    val requesterId = requester.id
    // Must coup if there are 10+ coins
    val requireCoup = requester.gameInfo.coins >= 10

    val newPlayerAction = when {
      action.targetsPlayer && recipient == null -> {
        val options = players.filter { it.id != requester.id && it.active }
          .map { it.lobbyPlayer.id }
        SelectingPlayer(
          options = options,
          requireCoup = requireCoup,
          action = if (requireCoup) CoupCardAction.Coup else action
        )
      }

      action.actingCard != null || action.blockingCards.isNotEmpty() -> {
        val pendingAction = PendingAction(action = action, requester = requester.lobbyPlayer.id, recipient = recipient)
        Validating(
          pendingAction = pendingAction, accepted = setOf()
        )
      }

      else -> Waiting
    }
    return copy(playerAction = newPlayerAction).thenIf({ playerAction is Waiting }) {
      act(requester = requester.id, action = action, recipient = recipient)
    }
  }

  private fun Coup.act(requester: PlayerId, action: CoupCardAction, recipient: PlayerId?): Coup {
    return when (action) {
      Income -> updatePlayer(requester) {
        copy(coins = coins + 1)
      }

      ForeignAid -> updatePlayer(requester) {
        copy(coins = coins + 2)
      }

      Tax -> updatePlayer(requester) {
        copy(coins = coins + 3)
      }

      Assassinate -> copy(
        playerAction = RequestingLoseCard(
          playerId = recipient!!,
          reason = Assassinated(requester = requester)
        )
      )

      CoupCardAction.Coup -> copy(
        playerAction = RequestingLoseCard(
          playerId = recipient!!,
          reason = Couped(requester = requester)
        )
      )

      Steal -> {
        val coinTransfer = findPlayer(recipient!!)?.gameInfo?.coins?.coerceAtMost(2) ?: return this
        val newPlayers = players.map { player ->
          when (player.id) {
            requester -> player.updateGameInfo { copy(coins = coins + coinTransfer) }
            recipient -> player.updateGameInfo { copy(coins = coins - coinTransfer) }
            else -> player
          }
        }
        copy(players = newPlayers)
      }

      Exchange -> {
        val playerCards = findPlayer(requester)!!.gameInfo.cards
        val cardOptions = playerCards + deck.take(2)
        copy(
          deck = deck.drop(2),
          playerAction = SelectingCard(playerId = requester, cards = cardOptions, count = playerCards.size)
        )
      }
    }
  }

  override fun playerState(state: Coup, playerId: PlayerId): CoupClient {
    TODO("Not yet implemented")
  }


}