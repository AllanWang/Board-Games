package ca.allanwang.game.lobby

import ca.allanwang.game.lobby.LobbyClient.Joined
import ca.allanwang.game.lobby.LobbyClient.NotJoined
import ca.allanwang.game.redux.StoreReducer

object LobbyReducer : StoreReducer<Lobby, LobbyClient, LobbyAction> {

  override fun reduce(
    state: Lobby, playerId: PlayerId, action: LobbyAction
  ): Lobby =
    when (action) {
      is Join -> {
        if (state.players.any { it.id == action.id }) {
          // Technically an error to create with the same id, but we will allow two computers to behave like one user
          // This is a no op
          state
        } else {
          state.copy(players = state.players + LobbyPlayer(id = action.id, active = true))
        }
      }

      Start -> {
        // Not handled here
        state
      }
    }

  override fun playerState(state: Lobby, playerId: PlayerId): LobbyClient {
    return if (state.players.any { it.id == playerId }) {
      Joined(code = state.code, self = playerId, players = state.players)
    } else {
      NotJoined
    }
  }

}