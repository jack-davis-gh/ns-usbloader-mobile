package com.github.jack_davis_gh.ns_usbloader.settings.ui

import android.net.InetAddresses
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.github.jack_davis_gh.ns_usbloader.ui.theme.AppTheme
import com.github.jack_davis_gh.ns_usbloader.ui.theme.ThemePreviews
import kotlinx.coroutines.flow.filter

class EditableSettingsState(initialValue: String, private val validation: Validation) {
    enum class Validation { IP, PORT }
    var text by mutableStateOf(initialValue)

    fun updateText(newText: String) {
        text = newText
    }

    val isError: Boolean
        get() = when (validation) {
            Validation.IP -> !InetAddresses.isNumericAddress(text)
            Validation.PORT ->
                (text.toIntOrNull() ?: 0) !in 1024..65535
        }

    companion object {
        val Saver: Saver<EditableSettingsState, *> = listSaver(
            save = { listOf(it.text, it.validation.name) },
            restore = { savedList ->
                EditableSettingsState(
                    initialValue = savedList[0],
                    validation = Validation.entries.find { it.name == savedList[1] } ?: throw IllegalStateException("Validation can't be null")
                )
            }
        )
    }
}

@Composable
fun rememberEditableSettingsState(initialValue: String, validation: EditableSettingsState.Validation) =
    rememberSaveable(initialValue, saver = EditableSettingsState.Saver) {
        EditableSettingsState(initialValue, validation)
    }

@Composable
internal fun TextNumberField(
    modifier: Modifier = Modifier,
    initialValue: String,
    validation: EditableSettingsState.Validation,
    onValidTextChange: (String) -> Unit,
    errorText: String = "",
    label: String,
    placeholder: String,
    enabled: Boolean = true
) {
    val state = rememberEditableSettingsState(initialValue, validation)
    TextNumberFieldContent(modifier, state, errorText, label, placeholder, enabled)
    val currentOnValidTextChange by rememberUpdatedState(onValidTextChange)
    LaunchedEffect(state) {
        snapshotFlow { state.text }
            .filter { !state.isError }
            .collect { currentOnValidTextChange(state.text) }
    }
}

@Composable
internal fun TextNumberFieldContent(
    modifier: Modifier = Modifier,
    state: EditableSettingsState = rememberEditableSettingsState("", EditableSettingsState.Validation.IP),
    errorText: String = "",
    label: String,
    placeholder: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        modifier = modifier,
        value = state.text,
        onValueChange = { newTextNum -> state.updateText(newTextNum) },
        supportingText = { if (state.isError) { Text(errorText) } },
        isError = state.isError,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        enabled = enabled
    )
}

@ThemePreviews
@Composable
fun TextNumberFieldPreview() {
    AppTheme {
        TextNumberField(
            label = "Ip Address",
            initialValue = "192.168.1.",
            validation = EditableSettingsState.Validation.IP,
            onValidTextChange = {},
            placeholder = "xxx.xxx.xxx.xxx"
        )
    }
}
