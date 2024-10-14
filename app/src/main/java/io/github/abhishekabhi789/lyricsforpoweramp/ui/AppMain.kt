package io.github.abhishekabhi789.lyricsforpoweramp.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.abhishekabhi789.lyricsforpoweramp.activities.SearchResultActivity
import io.github.abhishekabhi789.lyricsforpoweramp.ui.components.TopBar
import io.github.abhishekabhi789.lyricsforpoweramp.ui.search.SearchUi
import io.github.abhishekabhi789.lyricsforpoweramp.viewmodels.AppViewmodel
import kotlinx.coroutines.launch

@Composable
fun AppMain(modifier: Modifier = Modifier, viewModel: AppViewmodel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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
        topBar = { TopBar() },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
            .then(focusRemoverModifier)
            .background(color = MaterialTheme.colorScheme.surface)
    ) { paddingValues ->
        val result by viewModel.searchResults.collectAsState()
        SearchUi(
            viewModel = viewModel,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) { errMsg ->
            keyboardController?.hide()
            if (errMsg.isNullOrEmpty()) {
                val intent = Intent(context, SearchResultActivity::class.java).apply {
                    putParcelableArrayListExtra(SearchResultActivity.KEY_RESULT, ArrayList(result))
                    putExtra(SearchResultActivity.KEY_APP_THEME, viewModel.appTheme.value)
                    putExtra(
                        SearchResultActivity.KEY_POWERAMP_ID,
                        viewModel.inputState.value.queryTrack.realId
                    )
                }
                context.startActivity(intent)
            } else {
                scope.launch {
                    when (snackbarHostState.showSnackbar(
                        message = errMsg,
                        withDismissAction = true
                    )) {
                        SnackbarResult.Dismissed -> keyboardController?.show()
                        else -> {}
                    }
                }
            }
        }
    }
}
