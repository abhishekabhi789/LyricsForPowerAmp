package io.github.abhishekabhi789.lyricsforpoweramp.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import io.github.abhishekabhi789.lyricsforpoweramp.R
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
                            stringResource(R.string.top_bar_support),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { showMenu = false; openTipPage(context) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    })
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.top_bar_github_repo),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { showMenu = false; viewGithub(context) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
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
                    })
            }
        },
        modifier = modifier
    )
}

private fun openTipPage(context: Context) {
    val tipUrl = "https://ko-fi.com/X8X1V9VTH"
    openLink(context, tipUrl)
}

const val GITHUB_REPO_URL = "https://github.com/abhishekabhi789/LyricsForPowerAmp"
private fun viewGithub(context: Context) {
    openLink(context, GITHUB_REPO_URL)
}

private fun openLink(context: Context, link: String) {
    Intent(Intent.ACTION_VIEW, Uri.parse(link)).also { context.startActivity(it) }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}
