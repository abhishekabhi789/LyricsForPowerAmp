package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.FAB

@Composable
fun MakeLyricCards(
    lyrics: List<Lyrics>,
    sendToPowerAmp: Boolean,
    onLyricChosen: (Lyrics) -> Unit,
    onNavigateBack: () -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(300.dp),
        verticalItemSpacing = 16.dp,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        items(lyrics) { lyric ->
            LyricItem(
                lyrics = lyric,
                isLaunchedFromPowerAmp = sendToPowerAmp,
                onLyricChosen = onLyricChosen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
    FAB(onClick = onNavigateBack)
}