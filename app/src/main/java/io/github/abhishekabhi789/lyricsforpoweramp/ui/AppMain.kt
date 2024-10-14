package io.github.abhishekabhi789.lyricsforpoweramp.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.abhishekabhi789.lyricsforpoweramp.AppViewmodel
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.ui.components.TopBar
import io.github.abhishekabhi789.lyricsforpoweramp.ui.lyricslist.MakeLyricCards
import io.github.abhishekabhi789.lyricsforpoweramp.ui.search.SearchUi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AppMain(
    modifier: Modifier = Modifier,
    viewModel: AppViewmodel,
    onLyricChosen: (Lyrics, SnackbarHostState, NavController) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focusRemoverModifier = Modifier.clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        focusManager.clearFocus()
    }
    LaunchedEffect(snackbarHostState) {
        keyboardController.let { keyboard ->
            if (snackbarHostState.currentSnackbarData == null) keyboard?.show() else keyboard?.hide()
        }
    }
    Scaffold(
        topBar = { TopBar(scrollBehavior) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (navController.currentDestination?.route == AppScreen.List.name) {
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    elevation = FloatingActionButtonDefaults.elevation(),
                    modifier = Modifier.padding(all = 16.dp)
                ) {
                    Icon(Icons.Outlined.Search, stringResource(R.string.navigate_back_action))
                }
            }
        },
        modifier = modifier
            .then(focusRemoverModifier)
            .background(color = MaterialTheme.colorScheme.surface)
            .nestedScroll(scrollBehavior.nestedScrollConnection)

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
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + slideIntoContainer(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        towards = AnimatedContentTransitionScope.SlideDirection.Down
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + slideOutOfContainer(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        towards = AnimatedContentTransitionScope.SlideDirection.Up
                    )
                }
            ) {
                LaunchedEffect(Unit) {
                    scrollBehavior.state.contentOffset = 0f
                    scrollBehavior.state.heightOffset = 0f
                }
                SearchUi(viewModel = viewModel) { message ->
                    if (message.isNullOrEmpty())
                        navController.navigate(AppScreen.List.name) else {
                        keyboardController?.hide()
                        scope.launch {
                            when (snackbarHostState.showSnackbar(
                                message = message,
                                withDismissAction = true
                            )) {
                                SnackbarResult.Dismissed -> keyboardController?.show()
                                else -> {}
                            }
                        }
                    }
                }
            }
            composable(
                route = AppScreen.List.name,
                enterTransition = {
                    fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + slideIntoContainer(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        towards = AnimatedContentTransitionScope.SlideDirection.Up
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ) + slideOutOfContainer(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        towards = AnimatedContentTransitionScope.SlideDirection.Down
                    )
                }) {
                val results by viewModel.searchResults.collectAsState()
                val inputState by viewModel.inputState.collectAsState()
                val fromPowerAmp = remember(inputState) { inputState.queryTrack.realId != null }
                MakeLyricCards(
                    lyricsList = results,
                    sendToPowerAmp = fromPowerAmp,
                    onLyricChosen = { onLyricChosen(it, snackbarHostState, navController) }
                )
            }
        }
    }
}
