package  io.github.abhishekabhi789.lyricsforpoweramp.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextInputWithChips(
    fieldLabel: String,
    leadingIcon: ImageVector,
    initialValue: List<String>?,
    onInputChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var showClearWarningDialog: Boolean by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }
    val chipList: SnapshotStateList<String> = remember {
        mutableStateListOf(*initialValue?.toTypedArray() ?: emptyArray())
    }
    var isFocused by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(TextFieldDefaults.MinWidth, TextFieldDefaults.MinHeight)
            .border(
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.let { if (isFocused) it.secondary else it.outline }),
                shape = OutlinedTextFieldDefaults.shape
            )
            .clickable {
                focusRequester.requestFocus()
                isFocused = true
            }
            .onFocusChanged { state ->
                isFocused = state.hasFocus
            }
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 12.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            chipList.toList().forEach { chipText ->
                AssistChip(
                    label = { Text(text = chipText) },
                    onClick = { value = chipText },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                chipList.remove(chipText)
                                onInputChange(chipList)
                            },
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors()
                        .copy(labelColor = MaterialTheme.colorScheme.secondary)
                )
            }
            TextField(
                value = value,
                onValueChange = { value = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (value.isNotEmpty()) {
                        chipList.add(value)
                        onInputChange(chipList)
                        value = ""
                    } else keyboardController?.hide()
                }),
                singleLine = true,
                placeholder = { Text(text = fieldLabel) },
                colors = TextFieldDefaults.colors()
                    .copy(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                modifier = Modifier
                    .widthIn(min = 20.dp)
                    .focusRequester(focusRequester)
            )
        }
        if (chipList.isNotEmpty() || value.isNotEmpty()) {
            IconButton(
                onClick = {
                    if (value.isNotEmpty()) {
                        value = ""
                    } else showClearWarningDialog = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.clear_input),
                )
            }
        }

    }
    if (showClearWarningDialog) {
        ShowFieldClearWarning(
            fieldLabel = fieldLabel,
            onConfirm = {
                chipList.clear()
                onInputChange(chipList)
            },
            onDismiss = { showClearWarningDialog = false }
        )
    }

}

@Preview(showSystemUi = true)
@Composable
fun PreviewTextInputWithChips() {
    TextInputWithChips(
        fieldLabel = "Test Input",
        leadingIcon = Icons.Default.BugReport,
        initialValue = mutableListOf("Hello", "World"),
        onInputChange = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
