package ca.allanwang.game.coup

import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.BlockContestFailed
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.BlockContestSucceeded
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.ContestFailed
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.ContestSucceeded
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingAction
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingCard
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingPlayer
import ca.allanwang.game.coup.Coup.CoupPlayerAction.Validating
import ca.allanwang.game.coup.Coup.CoupPlayerAction.ValidatingBlock
import ca.allanwang.game.coup.Coup.PendingBlockAction
import ca.allanwang.game.coup.CoupClient.GameInfo.PlayerInfo
import ca.allanwang.game.lobby.PlayerId
import ca.allanwang.game.redux.StoreReducer

object CoupReducer : StoreReducer<Coup, CoupClient, CoupAction> {

  override fun reduce(
    state: Coup,
    playerId: PlayerId,
    action: CoupAction,
  ): Coup {
    val player = state.findPlayer(playerId) ?: return state
    val isCurrentActivePlayer = playerId == state.players[state.active].id
    val playerAction = state.playerAction
    // We never receive events when the player is out of the game
    if (!player.active) {
      return state
    }
    return when (action) {
      is SelectAction -> {
        if (!isCurrentActivePlayer) return state
        if (playerAction !is SelectingAction) return state
        val cardAction = action.action
        if (cardAction !in playerAction.options) return state
        state.selectAction(action = cardAction, recipient = null)
      }

      is SelectPlayer -> {
        if (!isCurrentActivePlayer) return state
        if (playerAction !is SelectingPlayer) return state
        if (action.player == playerId) return state
        val recipient = state.findPlayer(action.player) ?: return state
        state.selectAction(action = playerAction.action, recipient = recipient.id)
      }

      is AcceptAction -> {
        when (playerAction) {
          is Validating -> {
            val newAccepted = playerAction.accepted + player.id
            if (newAccepted.size == state.activeCount) {
              // Everyone accepted
              state.act(action = playerAction.pendingAction)
            } else {
              val newPlayerAction = playerAction.copy(accepted = playerAction.accepted + player.id)
              state.copy(playerAction = newPlayerAction)
            }
          }

          is ValidatingBlock -> {
            if (isCurrentActivePlayer) return state
            // Block complete; proceed with no action
            state.nextTurn()
          }

          else -> state
        }
      }

      is ContestAction -> {
        val contesterId = playerId
        when (playerAction) {
          is Validating -> {
            val card = playerAction.pendingAction.action.actingCard!!
            val requesterId = playerAction.pendingAction.requester

            if (state.checkPlayerCard(requesterId, card)) {
              // Contester loses card, requester switches cards
              state.switchCard(playerId = requesterId, card = card).copy(
                playerAction = RequestingLoseCard(
                  playerId = contesterId,
                  reason = ContestFailed(
                    pendingAction = playerAction.pendingAction
                  )
                )
              )
            } else {
              // Requester loses cards
              state.copy(
                playerAction = RequestingLoseCard(
                  playerId = requesterId,
                  reason = ContestSucceeded(
                    pendingAction = playerAction.pendingAction
                  )
                )
              )
            }
          }

          is ValidatingBlock -> {
            if (playerId != playerAction.pendingBlock.action.requester) {
              return state
            }
            val blockingPlayer = playerAction.pendingBlock.blockingPlayer
            val blockingCard = playerAction.pendingBlock.blockingCard
            if (state.checkPlayerCard(
                blockingPlayer,
                blockingCard
              )
            ) {
              // Contester (self) loses card, blocker switches card
              state.switchCard(playerId = blockingPlayer, card = blockingCard)
                .copy(
                  playerAction = RequestingLoseCard(
                    playerId = contesterId,
                    reason = BlockContestFailed(
                      pendingBlock = playerAction.pendingBlock
                    )
                  )
                )
            } else {
              // Contester succeeds, blocker loses card
              state.copy(playerAction = RequestingLoseCard(
                playerId = contesterId,
                reason = BlockContestSucceeded(
                  pendingBlock = playerAction.pendingBlock
                ))
              )
            }
          }
          else -> state
        }
      }

      is BlockAction -> {
        if (playerAction !is Validating) return state
        if (action.card !in playerAction.pendingAction.action.blockingCards)  return state
        if (playerId == playerAction.pendingAction.requester)  return state
        val newPlayerAction = ValidatingBlock(
          pendingBlock = PendingBlockAction(
            blockingPlayer = playerId,
            blockingCard = action.card,
            action = playerAction.pendingAction
          )
        )
        state.copy(playerAction = newPlayerAction)
      }

      is LoseCard -> {
        if (playerAction !is RequestingLoseCard)  return state
        if (playerId != playerAction.playerId)  return state
        if (!state.checkPlayerCard(playerId, action.card))  return state

        state.updatePlayer(playerId) {
          copy(cards = cards - action.card)
        }.postLoseCard(playerAction.reason)
      }

      is SelectCards -> {
        if (playerAction !is SelectingCard)  return state
        if (playerAction.playerId != playerId)  return state
        if (action.cards.size != playerAction.count)  return state
        val deckCards = playerAction.cards.toMutableList()
        for (card in action.cards) {
          if (!deckCards.remove(card))  return state
        }
        state.updatePlayer(playerAction.playerId) {
          copy(cards = action.cards)
        }.copy(deck = state.deck + deckCards.shuffled())
      }
    }
  }


  override fun playerState(state: Coup, playerId: PlayerId): CoupClient {
    // temporarily always return state of active player
    return CoupClient(
      self = playerId,
      active = state.active,
      players = state.players.map { it.lobbyPlayer },
      gameInfo = PlayerInfo(state.players[state.active].gameInfo),
      action = state.playerAction
    )
  }


}