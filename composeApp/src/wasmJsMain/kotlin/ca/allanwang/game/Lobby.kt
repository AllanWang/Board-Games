package ca.allanwang.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.allanwang.game.lobby.LobbyAction
import ca.allanwang.game.lobby.LobbyClient
import ca.allanwang.game.lobby.LobbyClient.Joined
import ca.allanwang.game.lobby.LobbyClient.NotJoined
import ca.allanwang.game.lobby.PlayerId

@Composable
internal fun Lobby(client: LobbyClient, dispatch: (LobbyAction) -> Unit) {
  when (client) {
    is Joined -> Lobby(client = client, dispatch = dispatch)
    NotJoined -> Join(onJoin = {
      dispatch(LobbyAction.Join(PlayerId(it)))
    })
  }
}

@Composable
private fun Lobby(client: Joined, dispatch: (LobbyAction) -> Unit) {
  SheetSurface {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (player in client.players) {
        Text(text = player.id.id, fontWeight = if (player.id == client.self) FontWeight.Bold else FontWeight.Normal)
      }
      Button( onClick = { dispatch(LobbyAction.Start) }) {
        Text("Start")
      }
    }
  }
}