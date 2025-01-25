package io.github.abhishekabhi789.lyricsforpoweramp.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.activities.AboutActivity
import io.github.abhishekabhi789.lyricsforpoweramp.activities.SettingsActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showMenu: Boolean by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.wrapContentSize()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Absolute.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = AnnotatedString(text = stringResource(R.string.app_name)),
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.secondary,
                                offset = Offset(1f, 1f),
                                blurRadius = 1f
                            )
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Cursive
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.top_bar_menu_descriptions),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.about),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        showMenu = false
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.top_bar_settings),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        showMenu = false
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}
