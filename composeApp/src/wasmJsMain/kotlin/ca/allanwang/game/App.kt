package ca.allanwang.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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

@Composable
fun App(state: State) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
      Game(state = state)
    }
    SelectionContainer(modifier = Modifier.fillMaxWidth()) {
      Text(text = state.state.toString(), minLines = 3)
    }
  }
}
