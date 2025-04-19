package ca.allanwang.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import boardgames.composeapp.generated.resources.Res
import boardgames.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
fun App() {
  MaterialTheme {
//    val navController = rememberNavController()
//    LaunchedEffect(Unit) {
//      window.bindToNavigation(navController)
//    }
    val scope = rememberCoroutineScope()
    val state = remember { State(scope = scope) }

//    NavHost(navController, startDestination = "/") {
//      composable("/") {App(state = state)  }
//      composable("/test") {
//        Text("test")
//      }
//    }

    App(state = state)
  }
}

private const val SOCKET_URL = "ws://localhost:8081"

@Composable
fun App(state: State) {
  Column(modifier = Modifier.fillMaxSize()) {
    var showContent by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
      Button(onClick = { showContent = !showContent }) {
        Text("Click me!")
      }
      AnimatedVisibility(showContent) {
        val greeting = remember { Greeting().greet() }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
          Image(painterResource(Res.drawable.compose_multiplatform), null)
          Text("Compose: $greeting")
        }
      }
      Button(onClick = {
//      client.get()
      }) {
        Text("Connect")
      }
    }
    Logs(modifier = Modifier.fillMaxWidth().weight(1f), logs = state.logs)
  }
}

@Composable
fun Logs(logs: List<String>, modifier: Modifier = Modifier) {
  LazyColumn(
    modifier = modifier, contentPadding = PaddingValues(
      16.dp
    )
  ) {
    items(logs) { line ->
      Text(text = line)
    }
  }
}