package io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyric
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.FAB

@Composable
fun MakeLyricCards(
    lyrics: List<Lyric>,
    fromPowerAmp: Boolean,
    onLyricChosen: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(lyrics) { lyric ->
            LyricItem(
                lyric = lyric,
                isLaunchedFromPowerAmp = fromPowerAmp,
                onLyricChosen = onLyricChosen)
        }
    }
    FAB(onClick = onNavigateBack)
}

@Preview(showSystemUi = true)
@Composable
fun LyricListPreview() {
    LyricsForPowerAmpTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MakeLyricCards(lyrics = makeDummyLyrics(), false, {}, {})
        }
    }
}

private fun makeDummyLyrics(): List<Lyric> {
    val json = """[
                       {
                          "name":"Track Name 1",
                          "trackName":"Track Title 1",
                          "artistName":"Artists Name 1",
                          "albumName":"Album Name 1",
                          "duration":200,
                          "instrumental":false,
                          "plainLyrics":"1 Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n Nunc sit amet turpis et odio egestas finibus vel quis nisi.\n Duis aliquam tortor non dui tempor, et sodales orci tempus.\n Mauris fermentum mauris quis commodo viverra.\n Suspendisse scelerisque lorem eu dolor fringilla ultrices.",
                          "syncedLyrics":"[00:10.00] 1 Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n [00:20.10] Nunc sit amet turpis et odio egestas finibus vel quis nisi.\n [00:30.20] Duis aliquam tortor non dui tempor, et sodales orci tempus.\n [00:40.30] Mauris fermentum mauris quis commodo viverra.\n [00:50.40] Suspendisse scelerisque lorem eu dolor fringilla ultrices."
                       },
                       {
                          "name":"Track Name 2",
                          "trackName":"Track Title 2 The Track Name 2",
                          "artistName":"Artists Name 2",
                          "albumName":"Album Name 2",
                          "duration":500,
                          "instrumental":false,
                          "plainLyrics":"2 Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n Nunc sit amet turpis et odio egestas finibus vel quis nisi.\n Duis aliquam tortor non dui tempor, et sodales orci tempus.\n Mauris fermentum mauris quis commodo viverra.\n Suspendisse scelerisque lorem eu dolor fringilla ultrices.",
                          "syncedLyrics":null
                          },
                          {
                          "name":"Track Name 3",
                          "trackName":"Track Title 3",
                          "artistName":"Artists Name 3",
                          "albumName":"Album Name 3",
                          "duration":15000,
                          "instrumental":false,
                          "plainLyrics":null,
                          "syncedLyrics":"[00:10.00] 3 Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n [00:20.10] Nunc sit amet turpis et odio egestas finibus vel quis nisi.\n [00:30.20] Duis aliquam tortor non dui tempor, et sodales orci tempus.\n [00:40.30] Mauris fermentum mauris quis commodo viverra.\n [00:50.40] Suspendisse scelerisque lorem eu dolor fringilla ultrices."
                           }
                       ]"""
    return Gson().fromJson(json, Array<Lyric>::class.java).toList()
}