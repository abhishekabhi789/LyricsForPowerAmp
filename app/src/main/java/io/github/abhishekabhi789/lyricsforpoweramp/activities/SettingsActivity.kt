package io.github.abhishekabhi789.lyricsforpoweramp.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.abhishekabhi789.lyricsforpoweramp.ui.settings.AppSettings
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import io.github.abhishekabhi789.lyricsforpoweramp.viewmodels.MainActivityViewModel


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainActivityViewModel = viewModel()
            LaunchedEffect(Unit) {
                val preferredTheme = AppPreference.getTheme(applicationContext)
                viewModel.updateTheme(preferredTheme)
            }
            val appTheme by viewModel.appTheme.collectAsState()
            LyricsForPowerAmpTheme(useDarkTheme = AppPreference.isDarkTheme(theme = appTheme)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    AppSettings(viewModel = viewModel, onClose = { finish() })
                }
            }
        }
    }
}
