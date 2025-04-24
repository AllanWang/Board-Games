package ca.allanwang.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import boardgames.composeapp.generated.resources.Res
import boardgames.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@Composable
fun App() {
  MaterialTheme {
    val navController = rememberNavController()
    // Does not work (runtime error)
    // LaunchedEffect(Unit) {
    //   window.bindToNavigation(navController)
    // }
    val scope = rememberCoroutineScope()
    val state = remember { State(scope = scope) }

    NavHost(navController, startDestination = "/") {
      composable("/") { App(state = state) }
      composable("/test") {
        Text("test")
      }
    }

    App(state = state)
  }
}

@OptIn(ExperimentalTime::class)
@Composable
fun App(state: State) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    var showContent by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
      val now = remember {
        System.now()
      }
      Text("Stamp $now")
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
      val scope = rememberCoroutineScope()
      Button(onClick = {
        scope.launch { state.connect() }
      }) {
        Text("Connect")
      }
    }
    Logs(modifier = Modifier.fillMaxWidth().weight(1f), state = state)
  }
}

@Composable
fun Logs(state: State, modifier: Modifier = Modifier) {
  val game by state.flow.collectAsState()
  SelectionContainer {
    LazyColumn(
      modifier = modifier, contentPadding = PaddingValues(
        16.dp
      )
    ) {
      item {
        Text(text = game.toString())
      }
      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
      items(state.logs) { line ->
        Text(text = line)
      }
    }
  }
}