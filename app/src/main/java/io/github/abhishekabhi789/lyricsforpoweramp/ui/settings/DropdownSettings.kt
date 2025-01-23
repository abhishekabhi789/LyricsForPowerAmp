package io.github.abhishekabhi789.lyricsforpoweramp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSettings(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    currentValue: T,
    values: List<T>,
    onSelection: (T) -> Unit,
    onExpandedChanged: (Boolean) -> Unit,
    getLabel: @Composable (T) -> String,
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChanged
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        ) {
            Text(
                text = getLabel(currentValue),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(4.dp))
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChanged(false) },
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(text = getLabel(value)) },
                    colors = MenuDefaults.itemColors()
                        .copy(
                            textColor = if (value == currentValue)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ),
                    onClick = {
                        onSelection(value)
                        onExpandedChanged(false)
                    },
                )
            }
        }
    }
}
