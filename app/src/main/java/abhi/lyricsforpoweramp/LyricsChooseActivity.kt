package abhi.lyricsforpoweramp

import abhi.lyricsforpoweramp.model.InputState
import abhi.lyricsforpoweramp.model.LyricsRequestState
import abhi.lyricsforpoweramp.ui.LyricViewModel
import abhi.lyricsforpoweramp.ui.lyricslist.MakeLyricCards
import abhi.lyricsforpoweramp.ui.search.SearchUi
import abhi.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import abhi.lyricsforpoweramp.ui.utils.TopBar
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maxmpz.poweramp.player.PowerampAPI
import java.lang.Thread.sleep

enum class AppScreen { Search, List; }

class LyricsChooseActivity : ComponentActivity() {
    private val applicationContext: ComponentActivity = this
    private lateinit var density: Density

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
    private fun LyricChooserApp(
        viewModel: LyricViewModel = viewModel(),
        navController: NavHostController = rememberNavController()
    ) {
        density = LocalDensity.current
        rememberCoroutineScope()
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_LYRICS_LINK -> {
                val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, PowerampAPI.NO_ID)
                val requestedTrack = PowerAmpIntentUtils.makeTrack(intent)
                viewModel.updateLyricsRequestDetails(
                    LyricsRequestState(
                        isLaunchedFromPowerAmp = true,
                        realId = realId,
                    )
                )
                viewModel.updateInputState(
                    InputState(
                        queryString = requestedTrack.trackName ?: "",
                        queryTrack = requestedTrack,
                        searchMode = InputState.SearchMode.Fine
                    )
                )
            }

            else -> {
                viewModel.updateLyricsRequestDetails(LyricsRequestState(isLaunchedFromPowerAmp = false))
            }
        }
        Scaffold(topBar = { TopBar(canNavigateBack = false, navigateUp = {}) }) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppScreen.Search.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = AppScreen.Search.name) {
                    SearchUi(
                        viewModel = viewModel,
                        onSearchComplete = { message ->
                            if (message.isNullOrEmpty())
                                navController.navigate(AppScreen.List.name) else {
                                message.toToast(applicationContext)
                            }
                        }
                    )
                }
                composable(route = AppScreen.List.name) {
                    val lyrics = viewModel.searchResults.collectAsState().value
                    val fromPowerAmp =
                        viewModel.lyricsRequestState.collectAsState().value.isLaunchedFromPowerAmp
                    MakeLyricCards(
                        lyrics = lyrics,
                        fromPowerAmp = fromPowerAmp,
                        onLyricChosen = { chosenLyrics ->
                            viewModel.chooseThisLyrics(applicationContext, chosenLyrics)
                            sleep(500) // when back to PA, lyrics should be there
                            applicationContext.finish()
                        },
                        onNavigateBack = { navController.popBackStack() })
                }
            }
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
}