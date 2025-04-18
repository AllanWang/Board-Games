package ca.allanwang.game.coup


sealed interface Action

data class SelectAction(val id: PlayerId, val requireCoup: Boolean) : Action

data class SelectPlayerAction(val id: PlayerId, val action: PlayerAction, val options: Set<PlayerId>) :
  Action

data class CheckContestAction(val id: PlayerId, val action: PlayerAction, val recipient: PlayerId?) : Action

data class ContestAction(val id: PlayerId, val card: Card) : Action

data class AcceptAction(val id: PlayerId) : Action

data class SelectCardAction(val id: PlayerId, val options: List<Card>, val count: Int) : Action

data class LoseCardAction(val id: PlayerId) : Action

enum class PlayerAction {
  Income, ForeignAid, Tax, Assassinate, Coup, Steal, Exchange
}
