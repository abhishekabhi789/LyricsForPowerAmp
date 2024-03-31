package io.github.abhishekabhi789.lyricsforpoweramp.ui.utils

import android.app.AlertDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.lyricsforpoweramp.R

@Composable
fun TextInput(
    label: String,
    icon: ImageVector,
    text: String?,
    isError: Boolean,
    isSingleLine: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    clearWithoutWarn: Boolean = true,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = text ?: "",
            onValueChange = { onValueChange(it) },
            label = { Text(label) },
            singleLine = isSingleLine,
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (text?.isNotEmpty() == true) {
                    Icon(imageVector = Icons.Outlined.Clear,
                        contentDescription = stringResource(R.string.clear_input),
                        modifier = Modifier.clickable {
                            if (clearWithoutWarn) onValueChange("")
                            else showFieldClearWarning(
                                context = context,
                                fieldLabel = label,
                                onConfirm = { onValueChange("") })
                        })
                } else if (isError) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = stringResource(R.string.error),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            supportingText = {
                if (isError) {
                    Text(
                        text = stringResource(R.string.invalid_input_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = isError,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction,
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun showFieldClearWarning(context: Context, fieldLabel: String, onConfirm: () -> Unit) {
    val builder = AlertDialog.Builder(context).apply {
        setTitle(context.getString(R.string.input_clear_confirmation_message, fieldLabel))
        setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
    }
    builder.create().show()
}

@Preview(showBackground = true)
@Composable
fun PreviewTextInput() {
    TextInput(
        label = "Enter an input",
        icon = Icons.Outlined.BugReport,
        text = "Lorem Ipsum",
        isError = false,
        onValueChange = {})
}