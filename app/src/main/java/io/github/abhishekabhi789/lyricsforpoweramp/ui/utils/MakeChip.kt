package io.github.abhishekabhi789.lyricsforpoweramp.ui.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.R

@Composable
fun MakeChip(
    label: String,
    selected: Boolean,
    icon: Any?,
    onClick: () -> Unit = {}
) {
    FilterChip(
        onClick = { onClick() },
        label = { Text(label) },
        leadingIcon = {
            if (icon != null) {
                if (icon is Int)
                    Icon(
                        painterResource(id = icon),
                        contentDescription = null,
                        Modifier.size(FilterChipDefaults.IconSize)
                    )
                else Icon(
                    imageVector = icon as ImageVector,
                    contentDescription = null,
                    Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        },
        colors = MaterialTheme.colorScheme.let { cs ->
            FilterChipDefaults.filterChipColors(
                containerColor = cs.secondaryContainer,
                selectedContainerColor = cs.primaryContainer,
//                labelColor = cs.onSecondary,
//                selectedLabelColor = cs.onPrimary,
//                iconColor = cs.onSecondaryContainer,
//                disabledLeadingIconColor = cs.onPrimaryContainer
            )
        },
        selected = selected,
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
            icon = R.drawable.ic_plain_lyrics,
            onClick = {}
        )
        MakeChip(
            label = stringResource(R.string.synced_lyrics),
            selected = false,
            icon = R.drawable.ic_synced_lyrics,
            onClick = {}
        )
    }
}
