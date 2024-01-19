package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.Lyric
import abhi.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.maxmpz.poweramp.player.PowerampAPI
import kotlinx.coroutines.runBlocking


class LyricsLinkActivity : ComponentActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var context: Context
    private var realId: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var lyrics: Array<Lyric>? = arrayOf()
        context = this.applicationContext
        when (intent.action) {
            PowerampAPI.Lyrics.ACTION_LYRICS_LINK -> {
                Toast.makeText(this, "Loading Lyrics...", Toast.LENGTH_SHORT).show()
                realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
                val track = PowerAmpIntentUtils.makeTrack(intent)
                runBlocking {
                    lyrics = LyricsApiHelper().getLyricsForTrack(track)
                }
                if (lyrics.isNullOrEmpty()) {
                    Log.e(TAG, "onCreate: No Lyrics")
                    Toast.makeText(this, "No Lyrics Found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        if (lyrics.isNullOrEmpty()) finish()
        setContent {
            LyricsForPowerAmpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MakeLyricCards(lyrics = lyrics!!)
                }
            }

        }
    }

    @Composable
    fun MakeLyricCards(lyrics: Array<Lyric>) {
        Scaffold(topBar = { TopBar() }) { it ->
            LazyColumn(
                contentPadding = it,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(lyrics) {
                    LyricItem(lyric = it, modifier = Modifier)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(modifier: Modifier = Modifier) {
        CenterAlignedTopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.wrapContentSize(Alignment.Center)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = typography.titleLarge,
                        fontFamily = FontFamily.Cursive
                    )
                }
            },
            modifier = modifier
        )
    }

    @Composable
    fun LyricItem(lyric: Lyric, modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                Row(modifier = Modifier.wrapContentHeight()) {
                    Text(
                        text = lyric.trackName,
                        style = typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    ChooseThisLyricsButton {
                        val sent = PowerAmpIntentUtils.sendLyricResponse(
                            context = context,
                            realId = realId,
                            lyrics = lyric.syncedLyrics
                        )
                        if (sent) finish() else Toast.makeText(
                            context,
                            "Failed to choose",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Text(text = lyric.getFormattedDuration())
                Text(
                    text = lyric.albumName,
                    style = typography.titleSmall,
                    modifier = Modifier
                )
                Text(
                    text = lyric.artistName,
                    style = typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Divider(modifier = Modifier.padding(4.dp))
                //availability of either synced or plain lyrics is ensured while parsing api response
                if (expanded) {
                    LyricViewer(
                        lyrics = lyric.syncedLyrics ?: lyric.plainLyrics!!
                    ) { expanded = !expanded }
                } else ClickableText(
                    text = AnnotatedString(lyric.plainLyrics ?: lyric.syncedLyrics!!),
                    onClick = { expanded = !expanded },
                    style = TextStyle(fontStyle = FontStyle.Italic),
                    maxLines = 5,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()

                )
            }
        }
    }

    @Composable
    private fun ChooseThisLyricsButton(onClick: () -> Unit) {
        IconButton(onClick = { onClick() }) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "choose this lyrics",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }

    @Composable
    fun LyricViewer(
        lyrics: String,
        onToggle: () -> Unit
    ) {
        ClickableText(
            text = AnnotatedString(lyrics),
            onClick = { onToggle() },
            style = typography.bodyMedium,
            modifier = Modifier
        )
    }

    @Preview(showSystemUi = true)
    @Composable
    fun LyricListPreview() {
        LyricsForPowerAmpTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val lyrics = makeDummyLyrics()
                MakeLyricCards(lyrics = lyrics)
            }
        }
    }

    private fun makeDummyLyrics(): Array<Lyric> {
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
        return Gson().fromJson(json, Array<Lyric>::class.java)
    }
}