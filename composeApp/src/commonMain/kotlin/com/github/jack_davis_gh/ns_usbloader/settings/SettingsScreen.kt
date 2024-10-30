package com.github.jack_davis_gh.ns_usbloader.settings

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
//import com.github.jack_davis_gh.ns_usbloader.R
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.core.model.AppSettings
import com.github.jack_davis_gh.ns_usbloader.settings.ui.EditableSettingsState
import com.github.jack_davis_gh.ns_usbloader.settings.ui.TextNumberField
import com.github.jack_davis_gh.ns_usbloader.settings.ui.ThemeSelectionDropdown
import com.github.jack_davis_gh.ns_usbloader.ui.theme.ThemePreviews
import com.github.jack_davis_gh.ns_usbloader.ui.theme.AppTheme
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

@Inject
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    settingsViewModel: () -> SettingsViewModel,
    onBackPressed: () -> Unit = {}
) {
    val viewModel = viewModel { settingsViewModel() }
    val state: SettingsUiState by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(modifier, state, viewModel.updateStore, onBackPressed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    state: SettingsUiState,
    callbacks: SettingsPrefsUpdateCallback,
    onBackPressed: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")
                }
            })
        }
    ) { contentPadding ->
        when(state) {
            SettingsUiState.Loading -> {
                Box(Modifier.padding(contentPadding).fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(200.dp).align(Alignment.Center),
                        strokeWidth = 10.dp
                    )
                }
            }
            is SettingsUiState.Success -> {
                val uiState = state as? SettingsUiState.Success ?: return@Scaffold
                SettingsContent(modifier.padding(contentPadding), uiState, callbacks)
            }
        }
    }
}

@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState.Success,
    callbacks: SettingsPrefsUpdateCallback
) {
    Column(
        modifier = modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ThemeSelectionDropdown(
            modifier = Modifier.fillMaxWidth(),
            themeSelection = state.appSettings.theme,
            onThemeChanged = callbacks::updateThemeSelection
        )

        val protoOptions = listOf(
            Triple(Protocol.USB, Icons.Outlined.Usb, "USB"), //stringResource(R.string.tf_usb)),
            Triple(Protocol.Network, Icons.Outlined.Wifi, "NET")//stringResource(R.string.tf_net))
        )

        Text(
            modifier = Modifier.padding(top = 36.dp),
            text = "Transfer Protocol"//stringResource(R.string.transfer_protocol)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            protoOptions.fastForEachIndexed { i, (proto, icon, label) ->
                val selected = proto == state.appSettings.activeProto
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
                    onClick = { callbacks.updateProtocol(proto) }
                )
            }
        }

        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 0.dp),
            text = "NET", //stringResource(R.string.tf_net),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )

        TextNumberField(
            modifier = Modifier.fillMaxWidth(),
            initialValue = state.appSettings.nsIp,
            validation = EditableSettingsState.Validation.IP,
            onValidTextChange = callbacks::updateNsIp,
            errorText = "Invalid IP",
            label = "nsIp",//stringResource(R.string.settings_nsIp),
            placeholder = "xxx.xxx.xxx.xxx"
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "autoIp")//stringResource(R.string.settings_autodtct_phn_ip))
            Switch(
                checked = state.appSettings.autoIp,
                onCheckedChange = callbacks::updateAutoIp
            )
        }

        TextNumberField(
            modifier = Modifier.fillMaxWidth(),
            initialValue = state.appSettings.phoneIp,
            validation = EditableSettingsState.Validation.IP,
            onValidTextChange = callbacks::updatePhoneIp,
            errorText = "Invalid IP",
            label = "phoneIp",//stringResource(R.string.settings_phone_ip),
            placeholder = "xxx.xxx.xxx.xxx",
            enabled = !state.appSettings.autoIp
        )

        val phonePort = remember { state.appSettings.phonePort.toString() }
        TextNumberField(
            modifier = Modifier.fillMaxWidth(),
            initialValue = phonePort,
            validation = EditableSettingsState.Validation.PORT,
            onValidTextChange = { callbacks.updatePhonePort(it.toInt()) },
            errorText = "Invalid Port, 1024 <= port <= 65535",
            label = "phonePort", //stringResource(R.string.settings_phone_port),
            placeholder = "1024-65535"
        )
    }
}


@ThemePreviews
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            state = SettingsUiState.Success(AppSettings.Default),
            callbacks = SettingsPrefsUpdateCallback.Empty,
//            state = SettingsUiState.Loading,
            onBackPressed = {}
        )
    }
}