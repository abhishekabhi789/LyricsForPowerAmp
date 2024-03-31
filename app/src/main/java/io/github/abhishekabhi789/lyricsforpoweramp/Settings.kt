package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.InterpreterMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.TextInput
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference.FILTER

class Settings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LyricsForPowerAmpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    AppSettings()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showSystemUi = true)
    @Composable
    fun AppSettings() {
        val context = LocalContext.current
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.top_bar_settings),
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(R.string.navigate_back_action)
                        )
                    }
                })


        }) { contentPadding ->
            Column(
                Modifier
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = stringResource(R.string.settings_filter_caption),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                FilterField(context, FILTER.TITLE_FILTER, icon = Icons.Default.MusicNote)
                FilterField(context, FILTER.ARTISTS_FILTER, icon = Icons.Default.InterpreterMode)
                FilterField(context, FILTER.ALBUM_FILTER, icon = Icons.Default.Album)
            }
        }
    }

    @Composable
    fun FilterField(context: Context, filter: FILTER, icon: ImageVector) {
        var value by remember { mutableStateOf(AppPreference.getFilter(context, filter)) }
        TextInput(
            label = filter.label,
            icon = icon,
            text = value,
            isSingleLine = false,
            imeAction = ImeAction.Default,
            clearWithoutWarn = false,
            isError = false
        ) {
            value = it
            AppPreference.setFilter(context, filter, it)
        }
    }
}