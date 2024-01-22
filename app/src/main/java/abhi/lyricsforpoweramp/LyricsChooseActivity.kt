package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.Lyric
import abhi.lyricsforpoweramp.model.Track
import abhi.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.InterpreterMode
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxmpz.poweramp.player.PowerampAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


class LyricsChooseActivity : ComponentActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var context: Context

    /**Needed to update the track on PowerAmp. Applicable only for [PowerampAPI.Lyrics.ACTION_LYRICS_LINK]*/
    private var realId: Long = 0L
    private var isLaunchedFromPowerAmp by Delegates.notNull<Boolean>()

    /**An instance of track used to remember user input on the [SearchUi]*/
    private var searchTrack: Track? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LyricsForPowerAmpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LyricChooserApp()
                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun LyricChooserApp() {
        context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var showSearchUi: Boolean by remember { mutableStateOf(true) }
        val searchResultsState = remember { MutableStateFlow<List<Lyric>>(emptyList()) }
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_LYRICS_LINK -> {
                isLaunchedFromPowerAmp = true
                realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
                searchTrack = PowerAmpIntentUtils.makeTrack(intent)
            }

            else -> {
                isLaunchedFromPowerAmp = false
            }
        }
        Scaffold(topBar = { TopBar() }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (showSearchUi) {
                    SearchUi(searchTrack, onSearch = { track ->
                        searchTrack = track
                        coroutineScope.launch {
                            try {
                                val results = getLyricsForTrack(track)
                                searchResultsState.value = results // Update the state
                                if (results.isEmpty()) {
                                    "No result found".toToast(context)
                                    Log.e(TAG, "LyricChooserApp: No Result found for $track", )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                }
                val searchResults by searchResultsState.collectAsState()
                if (searchResults.isNotEmpty()) {
                    MakeLyricCards(lyrics = searchResults)
                    showSearchUi = false
                }
            }
            if (!showSearchUi) {
                FAB {
                    searchResultsState.value = emptyList()
                    showSearchUi = true
                }
            }
        }
    }

    private suspend fun getLyricsForTrack(track: Track): MutableList<Lyric> {
        val lyrics = withContext(Dispatchers.IO) {
            LyricsApiHelper().getLyricsForTrack(track)
        }
        return lyrics ?: mutableListOf()
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
    fun FAB(onClick: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { onClick() },
                elevation = FloatingActionButtonDefaults.elevation(),
                modifier = Modifier
                    .padding(all = 16.dp)
                    .align(alignment = Alignment.BottomEnd),
            ) {
                Icon(Icons.Outlined.Search, "Show search UI button")
            }
        }
    }

    @Composable
    fun TextInput(
        label: String,
        icon: ImageVector,
        text: String?,
        isError: Boolean,
        onValueChange: (String?) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = text ?: "",
                onValueChange = { onValueChange(it) },
                label = { Text(label) },
                leadingIcon = { Icon(icon, contentDescription = null) },
                trailingIcon = {
                    if (text?.isNotEmpty() == true) {
                        Icon(imageVector = Icons.Outlined.Clear,
                            contentDescription = "clear input",
                            modifier = Modifier.clickable { onValueChange(null) })
                    } else if (isError) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = "error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                supportingText = {
                    if (isError) {
                        Text(
                            text = "Ensure a valid input",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = isError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    fun SearchUi(track: Track?, onSearch: (Track) -> Unit) {
        var trackTitle: String? by remember { mutableStateOf(track?.trackName) }
        var albumName: String? by remember { mutableStateOf(track?.albumName) }
        var artistName: String? by remember { mutableStateOf(track?.artistName) }
        //trackTitle is mandatory to get result.
        var emptyTitleInput: Boolean by remember { mutableStateOf(false) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TextInput(
                label = "Track Title",
                icon = Icons.Outlined.MusicNote,
                text = trackTitle,
                isError = emptyTitleInput
            ) {
                emptyTitleInput = false //resetting error on input
                trackTitle = it
            }
            TextInput(
                label = "Album Name",
                icon = Icons.Outlined.Album,
                text = albumName,
                isError = false//no need to use error on these fields
            ) {
                albumName = it
            }
            TextInput(
                label = "Artists",
                icon = Icons.Outlined.InterpreterMode,
                text = artistName,
                isError = false
            ) {
                artistName = it
            }
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedButton(onClick = {
                if (!trackTitle.isNullOrEmpty()) {
                    onSearch.invoke(Track(trackTitle, artistName, albumName, null, null))
                } else emptyTitleInput = true
            }) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "Search")
            }
        }
    }

    @Composable
    fun MakeLyricCards(lyrics: List<Lyric>) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            items(lyrics) {
                LyricItem(lyric = it, modifier = Modifier)
            }
        }
    }

    @Composable
    fun LyricItem(lyric: Lyric, modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var showSyncedLyrics by remember { mutableStateOf(showSyncedLyrics(lyric)) }
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    if (realId > 0) {
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
                }
                Text(
                    text = lyric.artistName,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = lyric.albumName,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = typography.titleSmall,
                    modifier = Modifier
                )
                Row {
                    Text(
                        text = lyric.getFormattedDuration(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (lyric.plainLyrics != null) {
                        MakeChip(
                            label = "Plain",
                            drawable = R.drawable.ic_plain_lyrics
                        ) { showSyncedLyrics = false }
                    }
                    if (lyric.syncedLyrics != null) {
                        MakeChip(
                            label = "Synced",
                            drawable = R.drawable.ic_synced_lyrics
                        ) { showSyncedLyrics = true }
                    }

                }
                Divider(modifier = Modifier.padding(4.dp))
                //availability of either synced or plain lyrics is ensured while parsing api response
                val currentLyrics =
                    (if (showSyncedLyrics) lyric.syncedLyrics else lyric.plainLyrics)!!
                if (expanded) {
                    LyricViewer(lyric = currentLyrics) { expanded = false }
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

    @Composable
    private fun MakeChip(label: String, @DrawableRes drawable: Int, onClick: () -> Unit) {
        AssistChip(
            onClick = { onClick() },
            label = { Text(label, style = typography.labelSmall) },
            leadingIcon = {
                Icon(
                    painterResource(id = drawable),
                    contentDescription = null,
                    Modifier.size(12.dp)
                )
            },
            modifier = Modifier
                .padding(horizontal = 2.dp)
        )
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
        lyric: String,
        onToggle: () -> Unit
    ) {
        ClickableText(
            text = AnnotatedString(lyric),
            onClick = { onToggle() },
            style = TextStyle(
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }

    private fun showSyncedLyrics(lyric: Lyric): Boolean {
        return if (isLaunchedFromPowerAmp) {
            //if launched from PowerAmp, prefer synced lyrics
            lyric.syncedLyrics != null //fallback use plain
        } else {
            //if launched from launcher, then prefer plain lyrics.
            lyric.plainLyrics == null//fallback use synced lyrics
        }
    }


    @Preview(showSystemUi = true)
    @Composable
    fun LyricListPreview() {
        LyricsForPowerAmpTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LyricChooserApp()
            }
        }
    }


    /**show the text as toast*/
    fun String.toToast(context: Context) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, this, Toast.LENGTH_LONG).show()
        }
    }
}