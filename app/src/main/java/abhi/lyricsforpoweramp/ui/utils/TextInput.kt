package abhi.lyricsforpoweramp.ui.utils

import abhi.lyricsforpoweramp.R
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextInput(
    label: String,
    icon: ImageVector,
    text: String?,
    isError: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = text ?: "",
            onValueChange = { onValueChange(it) },
            label = { Text(label) },
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
                        modifier = Modifier.clickable { onValueChange("") })
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
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTextInput() {
    TextInput(
        label = "Enter an input",
        icon = Icons.Outlined.BugReport,
        text = "Lorum Ipsum",
        isError = false,
        onValueChange = {})
}