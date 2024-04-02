package io.github.abhishekabhi789.lyricsforpoweramp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import io.github.abhishekabhi789.lyricsforpoweramp.model.InputState
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist.MakeLyricCards
import io.github.abhishekabhi789.lyricsforpoweramp.ui.search.SearchUi
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.TopBar
import kotlinx.coroutines.launch

const val CONTENT_ANIMATION_DURATION = 500

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
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        when (intent?.action) {
            PowerampAPI.Lyrics.ACTION_LYRICS_LINK -> {
                val requestedTrack = PowerAmpIntentUtils.makeTrack(this, intent)
                viewModel.updateLyricsRequestDetails(requestedTrack)
                viewModel.updateInputState(
                    InputState(
                        queryString = requestedTrack.trackName ?: "",
                        queryTrack = requestedTrack,
                        searchMode = if (requestedTrack.artistName.isNullOrEmpty() && requestedTrack.albumName.isNullOrEmpty())
                            InputState.SearchMode.Coarse else InputState.SearchMode.Fine
                    )
                )
            }

            else -> {
                viewModel.updateLyricsRequestDetails(Track())
            }
        }
        Scaffold(
            topBar = { TopBar() },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppScreen.Search.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(
                    route = AppScreen.Search.name,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(durationMillis = CONTENT_ANIMATION_DURATION)
                        ) + slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Down
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(durationMillis = CONTENT_ANIMATION_DURATION)
                        ) + slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Up
                        )
                    }
                ) {
                    SearchUi(
                        viewModel = viewModel,
                        onSearchComplete = { message ->
                            if (message.isNullOrEmpty())
                                navController.navigate(AppScreen.List.name) else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }
                    )
                }
                composable(
                    route = AppScreen.List.name,
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(durationMillis = CONTENT_ANIMATION_DURATION)
                        ) + slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Up
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(durationMillis = CONTENT_ANIMATION_DURATION)
                        ) + slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Down
                        )
                    }) {
                    val lyrics = viewModel.searchResults.collectAsState().value
                    val fromPowerAmp =
                        viewModel.inputState.collectAsState().value.queryTrack.realId != null
                    MakeLyricCards(
                        lyrics = lyrics,
                        sendToPowerAmp = fromPowerAmp,
                        onLyricChosen = { chosenLyrics ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Sending lyrics")
                            }
                            viewModel.chooseThisLyrics(applicationContext, chosenLyrics)
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