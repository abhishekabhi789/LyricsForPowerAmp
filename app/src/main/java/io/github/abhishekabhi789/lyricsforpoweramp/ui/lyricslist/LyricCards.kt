package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics

@Composable
fun MakeLyricCards(
    modifier: Modifier = Modifier,
    lyricsList: List<Lyrics>,
    sendToPowerAmp: Boolean,
    onLyricChosen: (Lyrics) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(230.dp),
        verticalItemSpacing = 8.dp,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(lyricsList) { lyric ->
            LyricItem(
                lyrics = lyric,
                isLaunchedFromPowerAmp = sendToPowerAmp,
                onLyricChosen = onLyricChosen,
            )
        }
    }
}
