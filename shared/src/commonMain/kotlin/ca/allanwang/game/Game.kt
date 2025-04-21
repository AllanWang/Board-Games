package ca.allanwang.game

import androidx.compose.runtime.Immutable
import ca.allanwang.game.coup.Coup
import ca.allanwang.game.coup.CoupAction
import ca.allanwang.game.coup.CoupClient
import ca.allanwang.game.coup.CoupReducer
import ca.allanwang.game.lobby.Lobby
import ca.allanwang.game.lobby.LobbyAction
import ca.allanwang.game.lobby.LobbyClient
import ca.allanwang.game.lobby.LobbyReducer
import ca.allanwang.game.lobby.PlayerId
import ca.allanwang.game.redux.Store
import ca.allanwang.game.redux.StoreReducer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Immutable
@Serializable
sealed interface Game

@Serializable
data object GameEmpty : Game

@Serializable
@JvmInline
value class GameLobby(val game: Lobby) : Game

@Serializable
@JvmInline
value class GameCoup(val game: Coup) : Game

@Immutable
@Serializable
sealed interface GameClient

@Serializable
data object GameClientEmpty : GameClient

@Serializable
@JvmInline
value class GameClientLobby(val client: LobbyClient) : GameClient

@Serializable
@JvmInline
value class GameClientCoup(val client: CoupClient) : GameClient


@Immutable
@Serializable
sealed interface GameAction

@Serializable
@JvmInline
value class GameActionLobby(val action: LobbyAction) : GameAction

@Serializable
@JvmInline
value class GameActionCoup(val action: CoupAction) : GameAction

class GameStore : Store<Game, GameClient, GameAction>(
  initialState = GameEmpty,
  playerStateReducer = GameReducer::playerState,
  reducer = GameReducer::reduce
)

object GameReducer : StoreReducer<Game, GameClient, GameAction> {

  override fun reduce(
    state: Game,
    playerId: PlayerId,
    action: GameAction,
  ): Game {

    return when {
      state is GameLobby && action is GameActionLobby ->
        GameLobby(LobbyReducer.reduce(state.game, playerId, action.action))

      state is GameCoup && action is GameActionCoup ->
        GameCoup(CoupReducer.reduce(state.game, playerId, action.action))

      else -> state
    }
  }

  override fun playerState(state: Game, playerId: PlayerId): GameClient {
    return when (state) {
      is GameEmpty -> GameClientEmpty

      is GameLobby ->
        GameClientLobby(LobbyReducer.playerState(state.game, playerId))

      is GameCoup ->
        GameClientCoup(CoupReducer.playerState(state.game, playerId))
    }
  }

}