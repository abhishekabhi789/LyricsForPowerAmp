package io.github.abhishekabhi789.lyricsforpoweramp.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.abhishekabhi789.lyricsforpoweramp.ui.about.AboutScreen
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LyricsForPowerAmpTheme {
                AboutScreen(onFinish = ::finish)
            }
        }
    }
}
