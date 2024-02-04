package abhi.lyricsforpoweramp.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FAB(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { onClick() },
            elevation = FloatingActionButtonDefaults.elevation(),
            modifier = Modifier
                .padding(all = 16.dp)
                .align(alignment = Alignment.BottomEnd),
        ) {
            Icon(Icons.Outlined.Search, "Show search UI FA button")
        }
    }
}
@Preview(showSystemUi = true)
@Composable
fun FabPreview(){
    FAB(onClick = { })
}