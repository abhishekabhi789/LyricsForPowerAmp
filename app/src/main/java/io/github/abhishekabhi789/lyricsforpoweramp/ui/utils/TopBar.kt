package io.github.abhishekabhi789.lyricsforpoweramp.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import io.github.abhishekabhi789.lyricsforpoweramp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Cursive
                )
            }
        },
        actions = {
            IconButton(onClick = { openTipPage(context) }) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Tip Button"
                )
            }
            IconButton(onClick = { viewGithub(context) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = stringResource(R.string.github_repo_button_description)
                )
            }
        },
        modifier = modifier
    )
}

private fun openTipPage(context: Context) {
    val TIP_URL = "https://ko-fi.com/X8X1V9VTH"
    openLink(context, TIP_URL)
}

private fun viewGithub(context: Context) {
    val GITHUB_REPO_URL = "https://github.com/abhishekabhi789/LyricsForPowerAmp"
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