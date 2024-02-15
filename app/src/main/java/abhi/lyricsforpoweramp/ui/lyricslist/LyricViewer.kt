package abhi.lyricsforpoweramp.ui.lyricslist

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle

@Composable
fun LyricViewer(
    lyric: String,
    onSwipe: (Boolean) ->Unit,
    onClick: () -> Unit
) {
    ClickableText(
        text = AnnotatedString(lyric),
        onClick = { onClick() },
        style = TextStyle(
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                when {
                    dragAmount.x > 0 -> {
                        onSwipe(true)
                    }
                    dragAmount.x < 0 -> {
                        onSwipe(false)
                    }
                }
            }
        }
    )
}