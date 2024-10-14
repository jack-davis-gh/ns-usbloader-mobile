package com.blogspot.developersu.ns_usbloader.settings

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews

@Composable
internal fun SettingsTextNumberField(
    modifier: Modifier = Modifier,
    initialValue: String = "",
    isValid: (String) -> Boolean = { true },
    onValidUpdate: (String) -> Unit = {},
    errorText: String = "",
    label: String,
    placeholder: String,
    enabled: Boolean = true
) {
    var textNumber by rememberSaveable { mutableStateOf(initialValue) }
    var isError by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier,
        value = textNumber,
        onValueChange = { newTextNum ->
            textNumber = newTextNum
            if (isValid(newTextNum)) {
                isError = false
                onValidUpdate(newTextNum)
            } else {
                isError = true
            }
        },
        supportingText = { if (isError) { Text(errorText) } },
        isError = isError,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        enabled = enabled
    )
}

@ThemePreviews
@Composable
fun SettingsTextNumberFieldPreview() {
    AppTheme {
        SettingsTextNumberField(
            label = "Ip Address",
            placeholder = "xxx.xxx.xxx.xxx",
            initialValue = "192.168.1.",
            isValid = String::isStringIpAddress,
        )
    }
}
