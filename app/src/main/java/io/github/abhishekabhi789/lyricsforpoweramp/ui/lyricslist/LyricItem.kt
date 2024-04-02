package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.CONTENT_ANIMATION_DURATION
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.MakeChip

@Composable
fun LyricItem(
    lyrics: Lyrics,
    isLaunchedFromPowerAmp: Boolean,
    onLyricChosen: (Lyrics) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showPlainLyrics by remember { mutableStateOf(showPlainLyrics(lyrics)) }
    //availability of either synced or plain lyrics is ensured while parsing api response
    val currentLyrics = (if (showPlainLyrics) lyrics.plainLyrics else lyrics.syncedLyrics)!!
    val checkPlainLyricsChip = currentLyrics == lyrics.plainLyrics
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        ) {
            Row(modifier = Modifier.wrapContentHeight()) {
                Text(
                    text = lyrics.trackName,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                if (isLaunchedFromPowerAmp) {
                    ChooseThisLyricsButton(onClick = { onLyricChosen(lyrics) })
                }
            }
            Text(
                text = lyrics.artistName,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = lyrics.albumName,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
            )
            Row {
                Text(
                    text = lyrics.getFormattedDuration(),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.weight(1f))
                if (lyrics.plainLyrics != null) {
                    MakeChip(
                        label = stringResource(R.string.plain_lyrics_short),
                        selected = checkPlainLyricsChip,
                        drawable = R.drawable.ic_plain_lyrics
                    ) { showPlainLyrics = true }
                }
                if (lyrics.syncedLyrics != null) {
                    MakeChip(
                        label = stringResource(R.string.synced_lyrics_short),
                        selected = !checkPlainLyricsChip,
                        drawable = R.drawable.ic_synced_lyrics
                    ) { showPlainLyrics = false }
                }

            }
            HorizontalDivider(modifier = Modifier.padding(4.dp))

            AnimatedContent(
                currentLyrics,
                transitionSpec = {
                    if (currentLyrics == lyrics.plainLyrics) {
                        slideInHorizontally(
                            animationSpec = tween(CONTENT_ANIMATION_DURATION),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(CONTENT_ANIMATION_DURATION),
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(CONTENT_ANIMATION_DURATION),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(CONTENT_ANIMATION_DURATION),
                            targetOffsetX = { fullWidth -> -fullWidth })
                    }
                },
                label = "Lyrics Animation"
            ) { currentLyrics ->
                if (expanded) {
                    LyricViewer(
                        lyric = currentLyrics,
                        onClick = { expanded = false })
                } else ClickableText(
                    text = AnnotatedString(currentLyrics),
                    onClick = { expanded = true },
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    maxLines = 5,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ChooseThisLyricsButton(onClick: () -> Unit) {
    IconButton(onClick = { onClick() }) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = stringResource(R.string.choose_lyrics_button_description),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

private fun showPlainLyrics(lyrics: Lyrics): Boolean {
    return lyrics.plainLyrics != null
}