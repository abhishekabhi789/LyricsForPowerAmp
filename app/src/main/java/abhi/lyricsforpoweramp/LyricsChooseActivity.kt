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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.maxmpz.poweramp.player.PowerampAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


class LyricsChooseActivity : ComponentActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var context: Context
    private val CONTENT_ANIMATION_DURATION = 500

    /**Needed to update the track on PowerAmp. Applicable only for [PowerampAPI.Lyrics.ACTION_LYRICS_LINK]*/
    private var realId: Long = 0L
    private var isLaunchedFromPowerAmp by Delegates.notNull<Boolean>()

    /**An instance of track used to remember user input on the [SearchUi]*/
    private var searchTrack: Track? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            LyricsForPowerAmpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    LyricChooserApp()
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun LyricChooserApp() {
        val density = LocalDensity.current
        context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var showSearchUi: Boolean by remember { mutableStateOf(true) }
        var isSearching: Boolean by remember { mutableStateOf(false) }
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
                AnimatedVisibility(
                    visible = showSearchUi,
                    enter = slideInVertically {
                        with(density) { -40.dp.roundToPx() }
                    } + expandVertically(
                        expandFrom = Alignment.Bottom
                    ) + fadeIn(
                        initialAlpha = 0.3f
                    ),
                    exit = slideOutVertically() + shrinkVertically()
                ) {
                    SearchUi(searchTrack, onSearch = { track ->
                        searchTrack = track
                        isSearching = true
                        coroutineScope.launch {
                            try {
                                val results = getLyricsForTrack(track)
                                searchResultsState.value = results
                                if (results.isEmpty()) {
                                    getString(R.string.no_result).toToast(context)
                                    Log.e(TAG, "LyricChooserApp: No Result found for $track")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isSearching = false
                            }
                        }
                    })
                    if (isSearching) {
                        Dialog(
                            onDismissRequest = { isSearching = false },
                            DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = false
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(White, shape = RoundedCornerShape(8.dp))
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                val searchResults by searchResultsState.collectAsState()
                if (searchResults.isNotEmpty()) {
                    MakeLyricCards(lyrics = searchResults)
                    showSearchUi = false
                }
            }

            Box {
                AnimatedVisibility(
                    visible = !showSearchUi,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FAB(
                        onClick = {
                            searchResultsState.value = emptyList()
                            showSearchUi = true
                        },
                    )
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
    fun FAB(onClick: () -> Unit, modifier: Modifier = Modifier) {
        Box(modifier = modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { onClick() },
                elevation = FloatingActionButtonDefaults.elevation(),
                modifier = Modifier
                    .padding(all = 16.dp)
                    .align(alignment = Alignment.BottomEnd),
            ) {
                Icon(Icons.Outlined.Search, "Show search UI FA button")
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
                leadingIcon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (text?.isNotEmpty() == true) {
                        Icon(imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(R.string.clear_input),
                            modifier = Modifier.clickable { onValueChange(null) })
                    } else if (isError) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = stringResource(R.string.error),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                supportingText = {
                    if (isError) {
                        Text(
                            text = stringResource(R.string.invalid_input_error),
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
                label = stringResource(R.string.track_title),
                icon = Icons.Outlined.MusicNote,
                text = trackTitle,
                isError = false//temporarily disabled since only q params is used
            ) {
                emptyTitleInput = false //resetting error on input
                trackTitle = it
            }
            TextInput(
                label = stringResource(R.string.album_name),
                icon = Icons.Outlined.Album,
                text = albumName,
                isError = false //no need to use error on these fields
            ) {
                albumName = it
            }
            TextInput(
                label = stringResource(R.string.artists),
                icon = Icons.Outlined.InterpreterMode,
                text = artistName,
                isError = false
            ) {
                artistName = it
            }
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedButton(onClick = {
//                if (!trackTitle.isNullOrEmpty()) {
                onSearch.invoke(Track(trackTitle, artistName, albumName, null, null))
//                } else emptyTitleInput = true
            }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search)
                )
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

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun LyricItem(lyric: Lyric, modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var showPlainLyrics by remember { mutableStateOf(showPlainLyrics(lyric)) }
        //availability of either synced or plain lyrics is ensured while parsing api response
        val currentLyrics = (if (showPlainLyrics) lyric.plainLyrics else lyric.syncedLyrics)!!
        val checkPlainLyricsChip = currentLyrics == lyric.plainLyrics
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
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
            ) {
                Row(modifier = Modifier.wrapContentHeight()) {
                    Text(
                        text = lyric.trackName,
                        color = MaterialTheme.colorScheme.primary,
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
                                getString(R.string.failed_to_send_lyrics_to_poweramp),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                Text(
                    text = lyric.artistName,
                    color = MaterialTheme.colorScheme.primary,
                    style = typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = lyric.albumName,
                    color = MaterialTheme.colorScheme.primary,
                    style = typography.titleSmall,
                    modifier = Modifier
                )
                Row {
                    Text(
                        text = lyric.getFormattedDuration(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (lyric.plainLyrics != null) {
                        MakeChip(
                            label = stringResource(R.string.plain_lyrics_short),
                            selected = checkPlainLyricsChip,
                            drawable = R.drawable.ic_plain_lyrics
                        ) { showPlainLyrics = true }
                    }
                    if (lyric.syncedLyrics != null) {
                        MakeChip(
                            label = stringResource(R.string.synced_lyrics_short),
                            selected = !checkPlainLyricsChip,
                            drawable = R.drawable.ic_synced_lyrics
                        ) { showPlainLyrics = false }
                    }

                }
                Divider(modifier = Modifier.padding(4.dp))

                AnimatedContent(
                    currentLyrics,
                    transitionSpec = {
                        if (currentLyrics == lyric.plainLyrics) {
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
                ) {
                    if (expanded) {
                        LyricViewer(lyric = it) { expanded = false }
                    } else ClickableText(
                        text = AnnotatedString(it),
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MakeChip(
        label: String,
        selected: Boolean,
        @DrawableRes drawable: Int,
        onClick: () -> Unit
    ) {
        FilterChip(
            onClick = { onClick() },
            label = { Text(label, style = typography.labelSmall) },
            leadingIcon = {
                Box(modifier = Modifier.animateContentSize(keyframes {
                    durationMillis = CONTENT_ANIMATION_DURATION / 2
                })) {
                    if (selected) {
                        Icon(
                            painterResource(id = drawable),
                            contentDescription = null,
                            Modifier.size(12.dp)
                        )
                    }
                }
            }, selected = selected,
            modifier = Modifier.padding(horizontal = 2.dp)
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

    private fun showPlainLyrics(lyric: Lyric): Boolean {
        return lyric.plainLyrics != null
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