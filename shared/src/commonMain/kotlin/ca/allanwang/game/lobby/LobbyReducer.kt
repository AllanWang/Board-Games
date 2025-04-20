package ca.allanwang.game.lobby

import ca.allanwang.game.lobby.JoinFailure.DuplicateName
import ca.allanwang.game.redux.StoreReducer

fun Lobby.handle(id: PlayerId, message: LobbyMessageClient): LobbyMessageServer =
  when (message) {
    is Join -> {
      if (message.name in players.map { it.name }) {
        JoinFailure(reason = DuplicateName(name = message.name))
      } else {
        val lobby = copy(players = players + LobbyPlayer(id = id, name = message.name))
        LobbyUpdate(lobby = lobby)
      }
    }

    is Start -> StartGame
  }

fun Lobby.reduce(message: LobbyMessageServer, onStart: (Lobby) -> Unit): Lobby =
  when (message) {
    is JoinFailure -> this
    is LobbyUpdate -> message.lobby
    StartGame -> {
      onStart(this)
      this
    }
  }

object LobbyReducer : StoreReducer<Lobby, LobbyClient, LobbyMessage> {

  override fun reduce(state: Lobby, playerId: PlayerId, action: LobbyMessage, dispatch: (LobbyMessage) -> Unit): Lobby =
    when (action) {
      is Join -> {
        if (action.name in state.players.map { it.name }) {
          JoinFailure(reason = DuplicateName(name = action.name))
          state
        } else {
          state.copy(players = state.players + LobbyPlayer(id = playerId, name = action.name))
        }
      }

      Start -> TODO()
      is JoinFailure -> TODO()
      is LobbyUpdate -> TODO()
      StartGame -> TODO()
    }

  override fun playerState(state: Lobby, playerId: PlayerId): LobbyClient {
    TODO("Not yet implemented")
  }

}