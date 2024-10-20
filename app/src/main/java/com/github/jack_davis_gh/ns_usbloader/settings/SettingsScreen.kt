package com.github.jack_davis_gh.ns_usbloader.settings

import android.net.InetAddresses
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.jack_davis_gh.ns_usbloader.R
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.core.model.Settings
import com.github.jack_davis_gh.ns_usbloader.settings.ui.SettingsTextNumberField
import com.github.jack_davis_gh.ns_usbloader.ui.theme.ThemePreviews
import com.github.jack_davis_gh.ns_usbloader.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState: SettingsUiState by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        modifier = modifier,
        state = uiState,
        callback = viewModel.updateCallbacks,
        onBackPressed = onBackPressed
    )
}

internal fun String.isStringIpAddress() = InetAddresses.isNumericAddress(this)
private fun Int.isPhonePort()
    = this in 1024..65535

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    state: SettingsUiState,
    callback: SettingsPrefsUpdateCallback = SettingsPrefsUpdateCallback.Empty,
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
        when(state) {
            SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.padding(contentPadding).fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(200.dp).align(Alignment.Center),
                        strokeWidth = 10.dp
                    )
                }
            }
            is SettingsUiState.Success -> {
                Column(
                    modifier = modifier.padding(contentPadding).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeSelection(
                        modifier = Modifier.fillMaxWidth(),
                        themeSelection = state.settings.appTheme,
                        onThemeChanged = callback::updateThemeSelection
                    )

                    val protoOptions = listOf(
                        Triple(Protocol.TinfoilUSB, Icons.Outlined.Usb, stringResource(R.string.tf_usb)),
                        Triple(Protocol.TinfoilNET, Icons.Outlined.Wifi, stringResource(R.string.tf_net)),
//                        Triple(Protocol.GoldLeafUSB, Icons.Outlined.Usb, "GoldLeaf USB")
                    )

                    Text(
                        modifier = Modifier.padding(top = 36.dp),
                        text = stringResource(R.string.transfer_protocol)
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        protoOptions.fastForEachIndexed { i, (proto, icon, label) ->
                            val selected = proto == state.settings.activeProto
                            SegmentedButton(
                                selected = selected,
                                shape = SegmentedButtonDefaults.itemShape(index = i, count = protoOptions.size),
                                icon = {
                                    SegmentedButtonDefaults.Icon(false) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                        )
                                    }
                                },
                                label = { Text(label, maxLines = 1) },
                                onClick = { callback.updateProtocol(proto) }
                            )
                        }
                    }

                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 0.dp),
                        text = stringResource(R.string.tf_net),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )

                    SettingsTextNumberField(
                        modifier = Modifier.fillMaxWidth(),
                        initialValue = state.settings.nsIp,
                        isValid = String::isStringIpAddress,
                        onValidUpdate = callback::updateNsIp,
                        errorText = "Invalid IP",
                        label = stringResource(R.string.settings_nsIp),
                        placeholder = "xxx.xxx.xxx.xxx"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.settings_autodtct_phn_ip))
                        Switch(
                            checked = state.settings.autoIp,
                            onCheckedChange = callback::updateAutoIp
                        )
                    }

                    SettingsTextNumberField(
                        modifier = Modifier.fillMaxWidth(),
                        initialValue = state.settings.phoneIp,
                        onValidUpdate = callback::updatePhoneIp,
                        isValid = String::isStringIpAddress,
                        errorText = "Invalid IP",
                        label = stringResource(R.string.settings_phone_ip),
                        placeholder = "xxx.xxx.xxx.xxx",
                        enabled = !state.settings.autoIp
                    )

                    SettingsTextNumberField(
                        modifier = Modifier.fillMaxWidth(),
                        initialValue = state.settings.phonePort.toString(),
                        onValidUpdate = { callback.updatePhonePort(it.toInt()) },
                        isValid = { it.toIntOrNull()?.isPhonePort() ?: false },
                        errorText = "Invalid Port, 1024 <= port <= 65535",
                        label = stringResource(R.string.settings_phone_port),
                        placeholder = "1024-65535"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelection(
    modifier: Modifier = Modifier,
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


@ThemePreviews
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            state = SettingsUiState.Success(
                Settings(
                    appTheme = 0,
                    activeProto = Protocol.TinfoilUSB,
                    nsIp = "192.168.1.42",
                    autoIp = true,
                    phoneIp = "192.168.1.142",
                    phonePort = 6024
                )
            ),
//            state = SettingsUiState.Loading,
            onBackPressed = {}
        )
    }
}