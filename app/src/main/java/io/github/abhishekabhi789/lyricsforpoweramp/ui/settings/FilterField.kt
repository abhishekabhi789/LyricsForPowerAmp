package io.github.abhishekabhi789.lyricsforpoweramp.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.abhishekabhi789.lyricsforpoweramp.ui.components.TextInputWithChips
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference.FILTER

@Composable
fun FilterField(
    modifier: Modifier = Modifier,
    filter: FILTER,
    icon: ImageVector
) {
    val context = LocalContext.current
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
