package ca.allanwang.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.allanwang.game.lobby.Lobby
import ca.allanwang.game.lobby.PlayerId

@Composable
internal fun Game(state: State) {
  when (val client = state.state) {
    is GameClientCoup -> TODO()
    GameClientEmpty -> Join(onJoin = {
      state.connect(PlayerId(it))
    })
    is GameClientLobby -> Lobby(client = client, dispatch = state::send)
  }
}

@Composable
private fun Join(onJoin: (String) -> Unit) {
  Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
    Column(modifier = Modifier.align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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