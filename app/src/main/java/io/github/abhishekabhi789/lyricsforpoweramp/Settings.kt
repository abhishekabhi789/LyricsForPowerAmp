package io.github.abhishekabhi789.lyricsforpoweramp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.InterpreterMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.TextInputWithChips
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference.FILTER

class Settings : ComponentActivity() {
    private lateinit var viewModel: LyricViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            viewModel = viewModel()
            val preferredTheme = AppPreference.getTheme(this)
            viewModel.updateTheme(preferredTheme)
            val appTheme by viewModel.appTheme.collectAsState()
            LyricsForPowerAmpTheme(useDarkTheme = AppPreference.isDarkTheme(theme = appTheme)) {
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                AppThemeSettings(Modifier.padding(horizontal = 16.dp))
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                GeneralSettings(Modifier.padding(horizontal = 16.dp))
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                FilterSettings(Modifier.padding(horizontal = 16.dp))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppThemeSettings(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        var expanded by remember { mutableStateOf(false) }
        var currentTheme by remember { mutableStateOf(AppPreference.getTheme(context)) }
        Title(
            label = stringResource(id = R.string.settings_app_theme_label),
            icon = Icons.Default.ColorLens,
            modifier = modifier
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Text(
                text = stringResource(R.string.settings_app_theme_description),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                TextField(
                    value = stringResource(id = currentTheme.label),
                    readOnly = true,
                    onValueChange = {},
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.colors().copy(
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                        focusedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    modifier = Modifier
                        .widthIn(100.dp, 200.dp)
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AppPreference.getThemes().forEach {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = it.label)) },
                            colors = MenuDefaults.itemColors()
                                .copy(
                                    textColor = if (it.label == currentTheme.label)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                ),
                            onClick = {
                                currentTheme = it
                                expanded = false
                                AppPreference.setTheme(context, it, viewModel)
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun GeneralSettings(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        var dummyLyricsForTracks by remember {
            mutableStateOf(AppPreference.getDummyForTracks(context))
        }
        var dummyLyricsForStreams by remember {
            mutableStateOf(AppPreference.getDummyForStreams(context))
        }

        Title(
            label = stringResource(R.string.settings_general_label),
            icon = Icons.Default.Build,
            modifier = modifier
        )
        SwitchSettings(
            label = stringResource(R.string.settings_dummy_lyrics_track),
            enabled = dummyLyricsForTracks,
            onChange = { dummyLyricsForTracks = it; AppPreference.setDummyForTracks(context, it) },
            modifier = modifier
        )
        SwitchSettings(
            label = stringResource(R.string.settings_dummy_lyrics_stream),
            enabled = dummyLyricsForStreams,
            onChange = {
                dummyLyricsForStreams = it
                AppPreference.setDummyForStreams(context, it)
            },
            modifier = modifier
        )
    }

    @Composable
    fun FilterSettings(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        Title(
            label = stringResource(R.string.settings_filter_label),
            icon = Icons.Default.FilterAlt,
            modifier = modifier
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text = stringResource(R.string.settings_filter_caption),
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
        )
        Spacer(modifier = Modifier.padding(8.dp))
        FilterField(
            context, FILTER.TITLE_FILTER, icon = Icons.Default.MusicNote, modifier = modifier
        )
        FilterField(
            context,
            FILTER.ARTISTS_FILTER,
            icon = Icons.Default.InterpreterMode,
            modifier = modifier
        )
        FilterField(
            context, FILTER.ALBUM_FILTER, icon = Icons.Default.Album, modifier = modifier
        )
    }

    @Composable
    fun FilterField(
        context: Context, filter: FILTER, icon: ImageVector, modifier: Modifier = Modifier
    ) {
        var filters by remember {
            mutableStateOf(
                AppPreference.getFilter(context, filter)?.lines()?.map { it.trim() }
                    ?.toMutableStateList() ?: mutableStateListOf()
            )
        }

        TextInputWithChips(
            fieldLabel = stringResource(id = filter.label),
            leadingIcon = icon,
            initialValue = filters,
            onInputChange = {
                filters = it.toMutableStateList()
                AppPreference.setFilter(
                    context,
                    filter,
                    it.let { if (it.isEmpty()) null else it.joinToString("\n") })
            },
            modifier = modifier.padding(vertical = 8.dp)
        )
    }

    @Composable
    fun Title(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
        Row(modifier = modifier) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    @Composable
    fun SwitchSettings(
        label: String,
        enabled: Boolean,
        modifier: Modifier = Modifier,
        onChange: (Boolean) -> Unit
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = enabled,
                onCheckedChange = onChange
            )
        }
    }
}