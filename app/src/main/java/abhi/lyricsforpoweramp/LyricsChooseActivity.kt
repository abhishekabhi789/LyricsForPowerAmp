package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.Lyric
import abhi.lyricsforpoweramp.model.Track
import abhi.lyricsforpoweramp.ui.lyricslist.MakeLyricCards
import abhi.lyricsforpoweramp.ui.search.SearchUi
import abhi.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import abhi.lyricsforpoweramp.ui.utils.FAB
import abhi.lyricsforpoweramp.ui.utils.TopBar
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
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
    private lateinit var density: Density

    /**Needed to update the track on PowerAmp. Applicable only for [PowerampAPI.Lyrics.ACTION_LYRICS_LINK]*/
    private var realId: Long = 0L
    private var isLaunchedFromPowerAmp by Delegates.notNull<Boolean>()

    /**used to remember user input on the [SearchUi]*/
    private var searchTrack: Track? = null
    private var searchQuery: String? = null
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

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun LyricChooserApp() {
        density = LocalDensity.current
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
        var coarseSearchMode: Boolean by remember { mutableStateOf(!isLaunchedFromPowerAmp) }
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
                    SearchUi(searchQuery,
                        searchTrack,
                        coarseSearchMode,
                        onModeChange = { coarseSearchMode = it },
                        onQueryChange = { searchQuery = it },
                        onQueryTrackChange = { searchTrack = it },
                        onSearch = { track ->
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
                    MakeLyricCards(lyrics = searchResults, componentActivity = this@LyricsChooseActivity, realId = realId)
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

    private suspend fun getLyricsForTrack(query: Any): MutableList<Lyric> {
        val lyrics = withContext(Dispatchers.IO) {
            LyricsApiHelper().getLyricsForTrack(query)
        }
        return lyrics ?: mutableListOf()
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
}