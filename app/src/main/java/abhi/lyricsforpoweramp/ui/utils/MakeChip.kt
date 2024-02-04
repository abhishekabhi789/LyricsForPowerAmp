package abhi.lyricsforpoweramp.ui.utils

import abhi.lyricsforpoweramp.CONTENT_ANIMATION_DURATION
import abhi.lyricsforpoweramp.R
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeChip(
    label: String,
    selected: Boolean,
    @DrawableRes drawable: Int,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = { onClick() },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Box(modifier = Modifier.animateContentSize(keyframes {
                durationMillis = CONTENT_ANIMATION_DURATION / 2
            })) {
                if (selected) {
                    Icon(
                        painterResource(id = drawable),
                        contentDescription = null,
                        Modifier.size(12.dp)
                    )
                }
            }
        }, selected = selected,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewChip() {
    Row {
        MakeChip(label = "Plain Lyrics", selected = true, drawable = R.drawable.ic_plain_lyrics) {

        }
        MakeChip(label = "Synced Lyrics", selected = false, drawable = R.drawable.ic_synced_lyrics) {

        }
    }

}