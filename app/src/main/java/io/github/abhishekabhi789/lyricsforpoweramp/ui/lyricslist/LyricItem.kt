package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.InterpreterMode
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.MakeChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricItem(
    lyrics: Lyrics,
    isLaunchedFromPowerAmp: Boolean,
    onLyricChosen: (Lyrics) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val lyricPages = listOfNotNull(lyrics.plainLyrics, lyrics.syncedLyrics)
    val pagerState = rememberPagerState(pageCount = { lyricPages.size }, initialPage = 0)
    var expanded by remember { mutableStateOf(false) }
    //availability of either synced or plain lyrics is ensured while parsing api response
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .wrapContentHeight()
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Audiotrack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(AssistChipDefaults.IconSize)
            )
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Outlined.InterpreterMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(AssistChipDefaults.IconSize)
            )
            Text(
                text = lyrics.artistName,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Outlined.Album,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(AssistChipDefaults.IconSize)
            )
            Text(
                text = lyrics.albumName,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(AssistChipDefaults.IconSize)
            )
            Text(
                text = lyrics.getFormattedDuration(),
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.weight(1f))
            if (lyrics.plainLyrics != null) {
                MakeChip(
                    label = stringResource(R.string.plain_lyrics_short),
                    selected = lyricPages[pagerState.currentPage] == lyrics.plainLyrics,
                    icon = R.drawable.ic_plain_lyrics
                ) { scope.launch { pagerState.animateScrollToPage(0) } }
            }
            if (lyrics.syncedLyrics != null) {
                MakeChip(
                    label = stringResource(R.string.synced_lyrics_short),
                    selected = lyricPages[pagerState.currentPage] == lyrics.syncedLyrics,
                    icon = R.drawable.ic_synced_lyrics
                ) { scope.launch { pagerState.animateScrollToPage(lyricPages.lastIndex) } }
            }

        }
        HorizontalDivider(modifier = Modifier.padding(4.dp))
        HorizontalPager(state = pagerState, modifier = modifier) {
            ClickableText(
                text = AnnotatedString(
                    if (expanded) lyricPages[it]
                    else lyricPages[it].lines().subList(0, 6).joinToString("\n")
                ),
                onClick = { expanded = !expanded },
                style = TextStyle(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .animateContentSize()
            )
        }
    }
}

@Composable
private fun ChooseThisLyricsButton(onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = stringResource(R.string.choose_lyrics_button_description),
        )
    }
}

@Preview
@Composable
fun PreviewLyricItem() {
    val data = Lyrics(
        trackName = "Track Title 1",
        artistName = "Artists Name 1",
        albumName = "Album Name 1",
        duration = 200.0,
        plainLyrics = "1 Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n Nunc sit amet turpis et odio egestas finibus vel quis nisi.\n Duis aliquam tortor non dui tempor, et sodales orci tempus.\n Mauris fermentum mauris quis commodo viverra.\n Suspendisse scelerisque lorem eu dolor fringilla ultrices.\n Suspendisse scelerisque lorem eu dolor fringilla ultrices.\n Suspendisse scelerisque lorem eu dolor fringilla ultrices.",
        syncedLyrics = "[00:10.00] 1 Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n [00:20.10] Nunc sit amet turpis et odio egestas finibus vel quis nisi.\n [00:30.20] Duis aliquam tortor non dui tempor, et sodales orci tempus.\n [00:40.30] Mauris fermentum mauris quis commodo viverra.\n [00:50.40] Suspendisse scelerisque lorem eu dolor fringilla ultrices.\n [01:00.50] Suspendisse scelerisque lorem eu dolor fringilla ultrices.\n [01:10.00] Suspendisse scelerisque lorem eu dolor fringilla ultrices."
    )
    LyricItem(lyrics = data, isLaunchedFromPowerAmp = true, onLyricChosen = {})
}
