package io.github.abhishekabhi789.lyricsforpoweramp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.InterpreterMode
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.github.abhishekabhi789.lyricsforpoweramp.ui.theme.LyricsForPowerAmpTheme
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.PermissionDialog
import io.github.abhishekabhi789.lyricsforpoweramp.ui.utils.TextInputWithChips
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference.FILTER


class Settings : ComponentActivity() {
    private lateinit var viewModel: LyricViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
    fun AppSettings(modifier: Modifier = Modifier) {
        val focusManager = LocalFocusManager.current
        val interactionSource = remember { MutableInteractionSource() }
        val focusRemoverModifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { focusManager.clearFocus() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            topBar = {
                LargeTopAppBar(
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
                    },
                    scrollBehavior = scrollBehavior,
                    modifier = focusRemoverModifier
                )
            },
            modifier = modifier
                .then(focusRemoverModifier)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { contentPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding)
                    .padding(horizontal = 8.dp)
            ) {
                AppThemeSettings()
                LyricsRequestSettings()
                FilterSettings()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppThemeSettings(modifier: Modifier = Modifier) {
        SettingsGroup(
            modifier = modifier,
            title = stringResource(id = R.string.settings_app_theme_label),
            icon = Icons.Default.ColorLens
        ) {
            val context = LocalContext.current
            var expanded by remember { mutableStateOf(false) }
            var currentTheme by remember { mutableStateOf(AppPreference.getTheme(context)) }
            BasicSettings(label = stringResource(R.string.settings_app_theme_description)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    ) {
                        Text(
                            text = stringResource(id = currentTheme.label),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
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
    }

    @SuppressLint("InlinedApi", "PermissionLaunchedDuringComposition")
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun LyricsRequestSettings(modifier: Modifier = Modifier) {
        SettingsGroup(
            modifier = modifier,
            title = stringResource(R.string.settings_lyrics_request_label),
            icon = Icons.Default.Lyrics
        ) {
            val context = LocalContext.current
            var hasNotificationPermission by rememberSaveable { mutableStateOf(false) }
            var askPermission by rememberSaveable { mutableStateOf(false) }
            var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
            val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
            hasNotificationPermission = when (permissionState.status) {
                PermissionStatus.Granted -> true
                is PermissionStatus.Denied -> false
            }
            LaunchedEffect(askPermission) {
                if (askPermission) {
                    if (permissionState.status.shouldShowRationale) {
                        showPermissionDialog = true
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                    askPermission = false // resetting
                }
            }
            if (showPermissionDialog) {
                PermissionDialog(
                    onConfirm = {
                        askPermission = false
                        showPermissionDialog = false
                        val intent =
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        startActivity(intent)
                    },
                    onDismiss = {
                        askPermission = false
                        showPermissionDialog = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.settings_permission_toast_denied),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
            var fallbackToSearch by remember {
                mutableStateOf(AppPreference.getSearchIfGetFailed(context))
            }
            BasicSettings(
                label = stringResource(id = R.string.settings_fallback_to_search_label),
                description = stringResource(id = R.string.settings_fallback_to_search_description),
                modifier = Modifier
            ) {
                Switch(
                    checked = fallbackToSearch,
                    onCheckedChange = {
                        fallbackToSearch = it
                        AppPreference.setSearchIfGetFailed(context, it)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            var showNotification by remember {
                mutableStateOf(AppPreference.getShowNotification(context))
            }
            BasicSettings(
                label = stringResource(id = R.string.settings_request_fail_notification_label),
                description = stringResource(id = R.string.settings_request_fail_notification_description),
                modifier = Modifier
            ) {
                Switch(
                    checked = showNotification,
                    onCheckedChange = {
                        showNotification = it
                        AppPreference.setShowNotification(context, it)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AnimatedVisibility(
                    visible = showNotification && !hasNotificationPermission,
                    enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    BasicSettings(
                        label = stringResource(R.string.settings_notification_permission_label),
                        description = stringResource(R.string.settings_notification_permission_description)
                    ) {
                        Button(
                            onClick = { askPermission = true },
                            enabled = !hasNotificationPermission
                        ) {
                            Text(stringResource(R.string.settings_permission_button_grant))
                        }
                    }
                }
            }
            var overwriteNotification by remember {
                mutableStateOf(AppPreference.getOverwriteNotification(context))
            }
            AnimatedVisibility(
                visible = showNotification,
                enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = slideOutVertically() + shrinkVertically() + fadeOut()
            ) {
                BasicSettings(
                    label = stringResource(id = R.string.settings_overwrite_existing_notification_label),
                    description = stringResource(id = R.string.settings_overwrite_existing_notification_description),
                    modifier = Modifier.alpha(if (hasNotificationPermission) 1.0f else 0.7f)
                ) {
                    Switch(
                        checked = overwriteNotification,
                        enabled = hasNotificationPermission,
                        onCheckedChange = {
                            overwriteNotification = it
                            AppPreference.setOverwriteNotification(context, it)
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun FilterSettings(modifier: Modifier = Modifier) {
        SettingsGroup(
            modifier = modifier,
            title = stringResource(R.string.settings_filter_label),
            icon = Icons.Default.FilterAlt
        ) {
            val context = LocalContext.current

            Text(
                text = stringResource(R.string.settings_filter_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                FilterField(context, FILTER.TITLE_FILTER, icon = Icons.Default.MusicNote)
                FilterField(context, FILTER.ARTISTS_FILTER, icon = Icons.Default.InterpreterMode)
                FilterField(context, FILTER.ALBUM_FILTER, icon = Icons.Default.Album)
            }
        }
    }

    @Composable
    fun SettingsGroup(
        modifier: Modifier = Modifier,
        title: String,
        icon: ImageVector,
        content: @Composable (ColumnScope.() -> Unit)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(8.dp)
        ) {
            Row {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            content.invoke(this)
        }
    }

    @Composable
    fun BasicSettings(
        modifier: Modifier = Modifier,
        label: String,
        description: String? = null,
        control: @Composable (() -> Unit)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            control.invoke()
        }
    }

    @Composable
    fun FilterField(
        context: Context,
        filter: FILTER,
        icon: ImageVector,
        modifier: Modifier = Modifier
    ) {
        var filters: SnapshotStateList<String> = remember {
            mutableStateListOf(
                *AppPreference.getFilter(context, filter)?.lines()?.map { it.trim() }
                    ?.toTypedArray() ?: emptyArray()
            )
        }
        TextInputWithChips(
            fieldLabel = stringResource(id = filter.label),
            leadingIcon = icon,
            initialValue = filters.toList(),
            onInputChange = {
                filters = it.toMutableStateList()
                AppPreference.setFilter(
                    context, filter,
                    it.let { if (it.isEmpty()) null else it.joinToString("\n") })
            },
            modifier = modifier
        )
    }
}
