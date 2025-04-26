package ca.allanwang.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SheetSurface(content: @Composable () -> Unit) {
  Surface(
    modifier = Modifier.padding(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = MaterialTheme.shapes.extraLarge
  ) {
    Box(modifier = Modifier.defaultMinSize(minWidth = 360.dp).padding(24.dp)) {
      content()
    }
  }
}