package com.blogspot.developersu.ns_usbloader.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blogspot.developersu.ns_usbloader.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit
) {
    SettingsScreen(
        modifier = modifier,
        themeSelection = viewModel.themeSelection,
        onThemeChanged = viewModel::updateThemeSelection,
        nsIpString = viewModel.nsIpString,
        onNsIpChanged = viewModel::updateNsIp,
        autoIpBool = viewModel.autoIpBool,
        onAutoIpChanged = viewModel::updateAutoIp,
        phoneIpString = viewModel.phoneIpString,
        onPhoneIpChanged = viewModel::updatePhoneIp,
        phonePortString = viewModel.phonePortString,
        onPhonePortChanged = viewModel::updatePort,
        onBackPressed = onBackPressed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    themeSelection: Int,
    onThemeChanged: (Int) -> Unit,
    nsIpString: String,
    onNsIpChanged: (String) -> Unit,
    autoIpBool: Boolean,
    onAutoIpChanged: (Boolean) -> Unit,
    phoneIpString: String,
    onPhoneIpChanged: (String) -> Unit,
    phonePortString: String,
    onPhonePortChanged: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Settings") }, navigationIcon = {
            IconButton(
                onClick = onBackPressed,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }) }
    ) {
        Column(
            modifier = modifier.padding(it).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeSelection(
                themeSelection = themeSelection,
                onThemeChanged = onThemeChanged
            )

            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp, bottom = 3.dp),
                text = stringResource(R.string.tf_net),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nsIpString,
                onValueChange = onNsIpChanged,
                label = { Text(text = stringResource(R.string.settings_nsIp)) },
                placeholder = { Text(text = "xxx.xxx.xxx.xxx") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.settings_autodtct_phn_ip))
                Switch(
                    checked = autoIpBool,
                    onCheckedChange = onAutoIpChanged
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phoneIpString,
                onValueChange = onPhoneIpChanged,
                label = { Text(text = stringResource(R.string.settings_phone_ip)) },
                placeholder = { Text(text = "xxx.xxx.xxx.xxx") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phonePortString,
                onValueChange = onPhonePortChanged,
                label = { Text(text = stringResource(R.string.settings_phone_port)) },
                placeholder = { Text(text = "1024-65535") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            SettingsTextField(
                state = SettingsTextFieldUiState(
                    value = phonePortString,
                    onValueChanged = onPhonePortChanged,
                ),
                label = stringResource(R.string.settings_phone_port),
                placeholder = "1024-65535",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



@Composable
private fun SettingsTextField(
    state: SettingsTextFieldUiState,
    label: String,
    placeholder: String,
    modifier: Modifier
) {
    OutlinedTextField(
        modifier = modifier,
        value = state.value,
        onValueChange = state.onValueChanged,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelection(
    modifier: Modifier = Modifier.fillMaxWidth(),
    themeSelection: Int,
    onThemeChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val themeText = listOf("Follow System", "Day", "Night")

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
            value = themeText[themeSelection],
            readOnly = true,
            onValueChange = {},
            singleLine = true,
            label = { Text(stringResource(R.string.settings_app_theme)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable),
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )


        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            themeText.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = { onThemeChanged(index) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        themeSelection = 0,
        onThemeChanged = {},
        nsIpString = "",
        onNsIpChanged = {},
        autoIpBool = true,
        onAutoIpChanged = {},
        phoneIpString = "",
        onPhoneIpChanged = {},
        phonePortString = "",
        onPhonePortChanged = {},
        onBackPressed = {}
    )
}