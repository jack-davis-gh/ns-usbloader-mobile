package com.github.jack_davis_gh.ns_usbloader.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import com.github.jack_davis_gh.ns_usbloader.R
import com.github.jack_davis_gh.ns_usbloader.core.model.AppSettings
import com.github.jack_davis_gh.ns_usbloader.ui.theme.AppTheme
import com.github.jack_davis_gh.ns_usbloader.ui.theme.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionDropdown(
    modifier: Modifier = Modifier,
    themeSelection: AppSettings.Theme,
    onThemeChanged: (AppSettings.Theme) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val themeText = listOf("Follow System", "Day", "Night")

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            value = themeText[themeSelection.ordinal],
            readOnly = true,
            onValueChange = {},
            singleLine = true,
            label = { Text("App Theme") }, //stringResource(R.string.settings_app_theme)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            themeText.filter { it != themeText[themeSelection.ordinal] }.forEach { text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        val themeInt = themeText.indexOf(text)
                        onThemeChanged(AppSettings.Theme.entries[themeInt])
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun ThemeSelectionDropdownPreview() {
    AppTheme {
        ThemeSelectionDropdown(
            themeSelection = AppSettings.Theme.FollowSystem,
            onThemeChanged = {}
        )
    }
}