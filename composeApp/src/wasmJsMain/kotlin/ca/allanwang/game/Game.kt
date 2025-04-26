package ca.allanwang.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.allanwang.game.lobby.PlayerId

@Composable
internal fun Game(state: State) {
  when (val client = state.state) {
    is GameClientCoup -> TODO()
    GameClientEmpty -> Join(onJoin = {
      state.connect(PlayerId(it))
    })

    is GameClientLobby -> Lobby(client = client.client, dispatch = state::send)
  }
}

@Composable
internal fun Join(onJoin: (String) -> Unit) {
  SheetSurface {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      var username by remember { mutableStateOf("TestId") }
      Text("Coup", style = MaterialTheme.typography.titleLarge)
      OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        label = { Text("Username") },
        singleLine = true
      )
      Button(enabled = username.isNotEmpty(), onClick = { onJoin(username) }) {
        Text("Join")
      }
    }
  }
}