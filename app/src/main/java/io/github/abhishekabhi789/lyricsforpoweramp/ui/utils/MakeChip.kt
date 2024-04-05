package io.github.abhishekabhi789.lyricsforpoweramp.ui.utils

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.CONTENT_ANIMATION_DURATION
import io.github.abhishekabhi789.lyricsforpoweramp.R

@Composable
fun MakeChip(
    label: String,
    selected: Boolean,
    @DrawableRes drawable: Int,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = { onClick() },
        label = { Text(label) },
        leadingIcon = {
            Box(modifier = Modifier.animateContentSize(keyframes {
                durationMillis = CONTENT_ANIMATION_DURATION / 2
            })) {
                if (selected) {
                    Icon(
                        painterResource(id = drawable),
                        contentDescription = null,
                        Modifier.size(FilterChipDefaults.IconSize)
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
        MakeChip(
            label = stringResource(R.string.plain_lyrics),
            selected = true,
            drawable = R.drawable.ic_plain_lyrics,
            onClick = {}
        )
        MakeChip(
            label = stringResource(R.string.synced_lyrics),
            selected = false,
            drawable = R.drawable.ic_synced_lyrics,
            onClick = {}
        )
    }
}