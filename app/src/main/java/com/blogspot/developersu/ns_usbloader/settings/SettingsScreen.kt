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
        nsIpString = viewModel.nsIpString,
        autoIpBool = viewModel.autoIpBool,
        phoneIpString = viewModel.phoneIpString,
        phonePortString = viewModel.phonePortString,
        isNsIpError = viewModel.isNsIpError,
        isPhoneIpError = viewModel.isPhoneIpError,
        isPhonePortError = viewModel.isPhonePortError,
        onThemeChanged = viewModel::updateThemeSelection,
        onNsIpChanged = viewModel::updateNsIp,
        onAutoIpChanged = viewModel::updateAutoIp,
        onPhoneIpChanged = viewModel::updatePhoneIp,
        onPhonePortChanged = viewModel::updatePort,
        onBackPressed = onBackPressed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    themeSelection: Int,
    nsIpString: String,
    autoIpBool: Boolean,
    phoneIpString: String,
    phonePortString: String,

    isNsIpError: Boolean,
    isPhoneIpError: Boolean,
    isPhonePortError: Boolean,

    onThemeChanged: (Int) -> Unit,
    onNsIpChanged: (String) -> Unit,
    onAutoIpChanged: (Boolean) -> Unit,
    onPhoneIpChanged: (String) -> Unit,
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
    ) { contentPadding ->
        Column(
            modifier = modifier.padding(contentPadding).padding(10.dp),
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

            SettingsTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nsIpString,
                onValueChanged = onNsIpChanged,
                isError = isNsIpError,
                label = stringResource(R.string.settings_nsIp),
                placeholder = "xxx.xxx.xxx.xxx"
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

            SettingsTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phoneIpString,
                onValueChanged = onPhoneIpChanged,
                isError = isPhoneIpError,
                label = stringResource(R.string.settings_phone_ip),
                placeholder = "xxx.xxx.xxx.xxx",
                enabled = !autoIpBool
            )

            SettingsTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phonePortString,
                onValueChanged = onPhonePortChanged,
                isError = isPhonePortError,
                label = stringResource(R.string.settings_phone_port),
                placeholder = "1024-65535"
            )
        }
    }
}

@Composable
private fun SettingsTextField(
    modifier: Modifier,
    value: String,
    onValueChanged: (String) -> Unit,
    isError: Boolean,
    label: String,
    placeholder: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChanged,
        isError = isError,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        enabled = enabled
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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            value = themeText[themeSelection],
            readOnly = true,
            onValueChange = {},
            singleLine = true,
            label = { Text(stringResource(R.string.settings_app_theme)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )


        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            themeText.filter { it != themeText[themeSelection] }.forEach { text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onThemeChanged(themeText.indexOf(text))
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
        nsIpString = "",
        autoIpBool = true,
        phoneIpString = "",
        phonePortString = "",

        isNsIpError = false,
        isPhoneIpError = false,
        isPhonePortError = false,

        onThemeChanged = {},
        onNsIpChanged = {},
        onAutoIpChanged = {},
        onPhoneIpChanged = {},
        onPhonePortChanged = {},
        onBackPressed = {}
    )
}