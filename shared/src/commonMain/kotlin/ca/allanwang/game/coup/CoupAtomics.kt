package ca.allanwang.game.coup

import ca.allanwang.game.coup.Coup.CoupPlayerAction.GameEnded
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.Assassinated
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.BlockContestFailed
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.BlockContestSucceeded
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.ContestFailed
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.ContestSucceeded
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.Couped
import ca.allanwang.game.coup.Coup.CoupPlayerAction.RequestingLoseCard.Reason
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingAction
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingCard
import ca.allanwang.game.coup.Coup.CoupPlayerAction.SelectingPlayer
import ca.allanwang.game.coup.Coup.CoupPlayerAction.Validating
import ca.allanwang.game.coup.Coup.CoupPlayerAction.Waiting
import ca.allanwang.game.coup.Coup.PendingAction
import ca.allanwang.game.coup.CoupCardAction.Assassinate
import ca.allanwang.game.coup.CoupCardAction.Exchange
import ca.allanwang.game.coup.CoupCardAction.ForeignAid
import ca.allanwang.game.coup.CoupCardAction.Income
import ca.allanwang.game.coup.CoupCardAction.Steal
import ca.allanwang.game.coup.CoupCardAction.Tax
import ca.allanwang.game.lobby.PlayerId
import ca.allanwang.game.redux.thenIf

internal fun Coup.findPlayer(playerId: PlayerId): Player? =
  players.firstOrNull { it.id == playerId && it.active }

internal val Coup.activeCount: Int
  get() = players.count { it.active }

internal fun Coup.updatePlayer(playerId: PlayerId, action: GamePlayerInfo.() -> GamePlayerInfo): Coup =
  copy(players = players.map { player ->
    if (player.id == playerId) player.updateGameInfo(action) else player
  })

internal fun Player.updateGameInfo(action: GamePlayerInfo.() -> GamePlayerInfo): Player =
  copy(gameInfo = gameInfo.action())

internal fun Coup.checkPlayerCard(playerId: PlayerId, card: Card): Boolean {
  val gameInfo = findPlayer(playerId)?.gameInfo ?: return false
  return card in gameInfo.cards
}

internal fun Coup.nextTurn(): Coup {
  val nextPlayer = generateSequence(active + 1) { it + 1 }.take(players.size - 1).map { it.rem(players.size) }
    .firstOrNull { players[it].active }
  if (nextPlayer == null) {
    // Game has ended
    val newPlayerAction = GameEnded(winner = players[active].id)
    return copy(playerAction = newPlayerAction)
  }
  return copy(active = active, playerAction = SelectingAction(options = CoupCardAction.entries.toList()))
    // If we need to coup, skip confirmation
    .thenIf(
      { players[active].gameInfo.coins >= 10 }
    ) {
      selectAction(action = CoupCardAction.Coup, recipient = null)
    }
}



internal fun Coup.switchCard(playerId: PlayerId, card: Card): Coup {
  val player = findPlayer(playerId) ?: return this
  if (!player.active) return this
  if (card !in player.gameInfo.cards) return this
  val newCards = player.gameInfo.cards - card + deck.take(1)
  return updatePlayer(playerId) {
    copy(cards = newCards)
  }.copy(deck = deck.drop(1) + card)
}

internal fun Coup.act(action: PendingAction): Coup =
  act(action = action.action, recipient = action.recipient)

internal fun Coup.act(action: CoupCardAction, recipient: PlayerId?): Coup {
  if (!players[active].active) {
    return this
  }
  val requester = players[active].id
  return when (action) {
    Income -> updatePlayer(requester) {
      copy(coins = coins + 1)
    }.nextTurn()

    ForeignAid -> updatePlayer(requester) {
      copy(coins = coins + 2)
    }.nextTurn()

    Tax -> updatePlayer(requester) {
      copy(coins = coins + 3)
    }.nextTurn()

    Assassinate -> {
      // Assassinate costs 3 coins
      val player = findPlayer(requester) ?: return this
      if (player.gameInfo.coins < 3) return this
      
      updatePlayer(requester) {
        copy(coins = coins - 3)
      }.copy(
        playerAction = RequestingLoseCard(
          playerId = recipient!!,
          reason = Assassinated(requester = requester)
        )
      )
    }

    CoupCardAction.Coup -> {
      // Coup costs 7 coins
      val player = findPlayer(requester) ?: return this
      if (player.gameInfo.coins < 7) return this
      
      updatePlayer(requester) {
        copy(coins = coins - 7)
      }.copy(
        playerAction = RequestingLoseCard(
          playerId = recipient!!,
          reason = Couped(requester = requester)
        )
      )
    }

    Steal -> {
      val coinTransfer = findPlayer(recipient!!)?.gameInfo?.coins?.coerceAtMost(2) ?: return this
      val newPlayers = players.map { player ->
        when (player.id) {
          requester -> player.updateGameInfo { copy(coins = coins + coinTransfer) }
          recipient -> player.updateGameInfo { copy(coins = coins - coinTransfer) }
          else -> player
        }
      }
      // After stealing, move to the next player's turn
      copy(players = newPlayers).nextTurn()
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

internal fun Coup.postLoseCard(reason: Reason): Coup {
  return when (reason) {
    is Assassinated, is Couped -> nextTurn()
    is ContestSucceeded, is BlockContestFailed -> nextTurn()
    is ContestFailed -> {
      act(reason.pendingAction)
    }
    is BlockContestSucceeded -> {
      act(reason.pendingBlock.action)
    }
  }
}

internal fun Coup.selectAction(action: CoupCardAction, recipient: PlayerId?): Coup {
  val requester = players[active]
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
      val pendingAction = PendingAction(action = action, requester = requester.id, recipient = recipient)
      Validating(
        pendingAction = pendingAction, accepted = setOf()
      )
    }

    else -> Waiting
  }
  return copy(playerAction = newPlayerAction).thenIf({ playerAction is Waiting }) {
    act(action = action, recipient = recipient)
  }
}
