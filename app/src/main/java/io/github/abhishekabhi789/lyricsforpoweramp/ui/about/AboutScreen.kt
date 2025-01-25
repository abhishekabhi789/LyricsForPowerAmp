package io.github.abhishekabhi789.lyricsforpoweramp.ui.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.abhishekabhi789.lyricsforpoweramp.BuildConfig
import io.github.abhishekabhi789.lyricsforpoweramp.R

@Preview(showSystemUi = true)
@Composable
fun PreviewAboutScreen() {
    AboutScreen { }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onFinish: () -> Unit) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.about)) },
            navigationIcon = {
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back_action)
                    )
                }
            })
    }) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.padding(top = 64.dp))
            if (isPreview) Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                colorFilter = ColorFilter.tint(Color.White), contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .scale(1.5f)
                    .background(colorResource(id = R.color.ic_launcher_background))

            )
            else {
                val appIcon = context.packageManager.getApplicationIcon(context.packageName)
                Image(
                    bitmap = appIcon.toBitmap().asImageBitmap(),
                    contentDescription = null
                )
            }
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "${stringResource(R.string.version)}:  ${BuildConfig.VERSION_NAME}",
            )
            TextButton(onClick = {
                context.startActivity(
                    Intent(context, OssLicensesMenuActivity::class.java)
                )
            }) {
                Text(text = stringResource(R.string.third_party_licenses))
            }
        }
    }
}
