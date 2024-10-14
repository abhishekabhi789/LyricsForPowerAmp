package io.github.abhishekabhi789.lyricsforpoweramp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.model.InputState
import io.github.abhishekabhi789.lyricsforpoweramp.model.Lyrics
import io.github.abhishekabhi789.lyricsforpoweramp.ui.AppMain
import io.github.abhishekabhi789.lyricsforpoweramp.ui.components.PermissionDialog
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AppViewmodel

    @SuppressLint("InlinedApi")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            val scope = rememberCoroutineScope()
            viewModel = viewModel()
            /* should not ask from here if user disabled notifications from settings*/
            val shouldAskForNotificationPermission = rememberSaveable {
                AppPreference.getShowNotification(this@MainActivity)
            }
            val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
            var showPermissionDialog by rememberSaveable { mutableStateOf(!permissionState.status.isGranted) }
            if (shouldAskForNotificationPermission && showPermissionDialog) {
                PermissionDialog(
                    allowDisabling = true,
                    onConfirm = {
                        showPermissionDialog = false
                        if (permissionState.status.shouldShowRationale) {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                            }
                            startActivity(intent)
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    },
                    onDismiss = { disableNotification ->
                        if (disableNotification) {
                            AppPreference.setShowNotification(this@MainActivity, false)
                            Toast(this@MainActivity).apply {
                                setText(R.string.settings_permission_toast_denied)
                                setDuration(Toast.LENGTH_SHORT)
                            }.show()
                        }
                        showPermissionDialog = false
                    }
                )
            }
            LaunchedEffect(Unit) {
                viewModel.updateTheme(AppPreference.getTheme(this@MainActivity))
            }
            val appTheme by viewModel.appTheme.collectAsState()
            LyricsForPowerAmpTheme(useDarkTheme = AppPreference.isDarkTheme(theme = appTheme)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    when (intent?.action) {
                        PowerampAPI.Lyrics.ACTION_LYRICS_LINK, LyricsRequestReceiver.MANUAL_SEARCH_ACTION -> {
                            val requestedTrack = PowerAmpIntentUtils.makeTrack(this, intent)
                            viewModel.updateInputState(
                                InputState(
                                    queryString = requestedTrack.trackName ?: "",
                                    queryTrack = requestedTrack,
                                    searchMode = if (requestedTrack.artistName.isNullOrEmpty() && requestedTrack.albumName.isNullOrEmpty())
                                        InputState.SearchMode.Coarse else InputState.SearchMode.Fine
                                )
                            )
                        }
                    }
                    AppMain(
                        viewModel = viewModel,
                        onLyricChosen = { chosenLyrics, snackbarHostState, navController ->
                            scope.launch {
                                sendLyrics(snackbarHostState, chosenLyrics) {
                                    when (intent.action) {
                                        PowerampAPI.Lyrics.ACTION_LYRICS_LINK, LyricsRequestReceiver.MANUAL_SEARCH_ACTION -> {
                                            finish()
                                        }

                                        else -> navController.navigateUp()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun sendLyrics(
        snackbarHostState: SnackbarHostState,
        chosenLyrics: Lyrics, onComplete: () -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val sent = viewModel.chooseThisLyrics(applicationContext, chosenLyrics)
            if (sent) {
                Log.d(TAG, "sendLyrics: sent")
                when (snackbarHostState.showSnackbar(
                    message = "Success",
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )) {
                    SnackbarResult.Dismissed -> onComplete()
                    else -> {}
                }
            } else {
                Log.d(TAG, "sendLyrics: failed")
                when (snackbarHostState.showSnackbar(
                    "Failed to send lyrics",
                    "Retry",
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )) {
                    SnackbarResult.ActionPerformed -> sendLyrics(
                        snackbarHostState,
                        chosenLyrics,
                        onComplete
                    )

                    SnackbarResult.Dismissed -> onComplete()
                }
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
        val preferredTheme = AppPreference.getTheme(this)
        viewModel.updateTheme(preferredTheme)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
