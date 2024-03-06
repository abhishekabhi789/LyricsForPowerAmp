package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle

@Composable
fun LyricViewer(
    lyric: String,
    onClick: () -> Unit
) {
    ClickableText(
        text = AnnotatedString(lyric),
        onClick = { onClick() },
        style = TextStyle(
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
    )
}