package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.InterpreterMode
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.CustomChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyricItem(
    modifier: Modifier = Modifier,
    lyrics: Lyrics,
    isLaunchedFromPowerAmp: Boolean,
    onLyricChosen: (Lyrics) -> Unit
) {
    val scope = rememberCoroutineScope()
    val lyricPages = remember { listOfNotNull(lyrics.plainLyrics, lyrics.syncedLyrics) }
    val pagerState = rememberPagerState(pageCount = { lyricPages.size }, initialPage = 0)
    var expanded by remember { mutableStateOf(false) }
    val showExpandIcon by remember {
        derivedStateOf {
            if (pagerState.isScrollInProgress) pagerState.targetPage == pagerState.currentPage
            else true
        }
    }
    ElevatedCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Audiotrack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
                Text(
                    text = lyrics.trackName,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isLaunchedFromPowerAmp) {
                    FilledTonalIconButton(
                        colors = IconButtonDefaults.filledTonalIconButtonColors()
                            .copy(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                        onClick = { onLyricChosen(lyrics) }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.choose_lyrics_button_description),
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.InterpreterMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
                Text(
                    text = lyrics.artistName,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Album,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
                Text(
                    text = lyrics.albumName,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                    Text(
                        text = lyrics.getFormattedDuration(),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                FlowRow {
                    if (lyrics.plainLyrics != null) {
                        CustomChip(
                            label = stringResource(R.string.plain_lyrics_short),
                            selected = lyricPages[pagerState.currentPage] == lyrics.plainLyrics,
                            icon = R.drawable.ic_plain_lyrics
                        ) { scope.launch { pagerState.animateScrollToPage(0) } }
                    }
                    if (lyrics.syncedLyrics != null) {
                        CustomChip(
                            label = stringResource(R.string.synced_lyrics_short),
                            selected = lyricPages[pagerState.currentPage] == lyrics.syncedLyrics,
                            icon = R.drawable.ic_synced_lyrics
                        ) { scope.launch { pagerState.animateScrollToPage(lyricPages.lastIndex) } }
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(4.dp))
        Box(contentAlignment = Alignment.TopEnd) {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.animateContentSize()
            ) { pageIndex ->
                Text(
                    text = if (expanded) lyricPages[pageIndex]
                    else lyricPages[pageIndex].lines().subList(0, 6).joinToString("\n"),
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable(interactionSource = null, indication = null) {
                            expanded = !expanded
                        }
                )
            }

            this@ElevatedCard.AnimatedVisibility(visible = showExpandIcon) {
                val rotationAnimation = animateFloatAsState(
                    targetValue = if (expanded) -180f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "expand icon rotation animation"
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp, top = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .rotate(rotationAnimation.value)
                        .animateEnterExit(enter = scaleIn(), exit = scaleOut())

                )
            }
        }
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
