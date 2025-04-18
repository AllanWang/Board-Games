package ca.allanwang.game.coup

data class Coup(
    val players: List<Player>
)

data class Player(
    val id: String,
    val name: String,
    val coins: Int,
    val cards: List<Card>,
)

enum class Card {
    Ambassador, Assassin, Captain, Contessa, Duke,
}

sealed interface Actions {
    data object Income : Actions
    data object ForeignAid : Actions
    data object Tax : Actions
    data class Steal(val playerId: Int) : Actions
    data object Ambassador : Actions
    data class PickFromAmbassador(val cards: List<Card>): Actions
    data class Assassinate(val playerId: Int) : Actions
    data class Coup(val playerId: Int): Actions

}

sealed interface Reactions {
    data object BlockForeignAid: Reactions
    data object BlockTax: Reactions
    data class BlockSteal(val isCaptain: Boolean) : Reactions
    data object BlockAssassin : Reactions
    data object ContestCard : Reactions
    data object Accept: Reactions
}
//
//fun Coup.action(action: Actions): Reactions {
//
//}
//
//fun Coup.reaction(activePlayerId: String, action: Actions, reactions: Reactions): Coup {
//    when (action) {
//        is Actions.Income -> copy(players = players.map { player -> if (player.id == activePlayerId) player.copy(coins = player.coins + 1) else player } )
//        is Actions.ForeignAid -> {
//            if (reactions is Reactions.Accept) {
//                copy(players = players.map { player -> if (player.id == activePlayerId) player.copy(coins = player.coins + 2) else player })
//            } else {
//
//            }
//        }
//    }
//}
//
