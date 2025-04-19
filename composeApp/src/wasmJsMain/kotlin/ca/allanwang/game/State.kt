package ca.allanwang.game

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

@Immutable
class State(private val scope: CoroutineScope) {
  var logs: List<String> by mutableStateOf(listOf("Logs Here"))
    private set

//  val client: HttpClient =
//    HttpClient(Js) {
//      install(WebSockets)
//    }

  fun addLog(text: String) {
    logs += text
  }
}