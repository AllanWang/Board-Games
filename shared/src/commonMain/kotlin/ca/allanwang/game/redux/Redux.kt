package ca.allanwang.game.redux

import ca.allanwang.game.lobby.PlayerId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

typealias PlayerState<S, T> = (S, PlayerId) -> T

typealias Reducer<S, A> = (S, A) -> S

abstract class Store2<S, P, A>(
  initialState: S,
  private val playerStateReducer: PlayerState<S, P>,
  private val reducer: Reducer<S, A>,
) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  private val stateFlow = MutableStateFlow(initialState)

  val state: S get() = stateFlow.value

  fun dispatch(action: A) {
    stateFlow.update {
      reducer(it, action)
    }
  }

  fun observe(id: PlayerId): Flow<P> = stateFlow.map {
    playerStateReducer(it, id)
  }.distinctUntilChanged()
}

interface StoreReducer<S, P, A, C> {
  fun reduce(state: S,playerId: PlayerId, action: A, dispatch: (A) -> Unit, clientDispatch: (C) -> Unit): S

  fun playerState(state: S, playerId: PlayerId): P
}

fun <T> T.thenIf(condition: T.() -> Boolean, action: T.() -> T) = if (condition()) action() else this