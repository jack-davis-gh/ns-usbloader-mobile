package com.blogspot.developersu.ns_usbloader.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import com.blogspot.developersu.ns_usbloader.core.model.Protocol
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile

sealed interface HomeScreenState {
    data class Success(
        val activeProtocol: Protocol,
        val files: List<NSFile>,
        val selectedFileNum: Int
    ): HomeScreenState
    data object Loading: HomeScreenState
}

interface HomeScreenCallbacks {
    fun onDeleteSelected()
    fun onClearSelected()
    fun onFileClicked(file: NSFile)
    fun onUploadFileClicked()
    fun onFileUriSelected(file: PlatformFile)

    object Default: HomeScreenCallbacks {
        override fun onDeleteSelected() {}
        override fun onClearSelected() {}
        override fun onFileClicked(file: NSFile) {}
        override fun onUploadFileClicked() {}
        override fun onFileUriSelected(file: PlatformFile) {}
    }
}

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSettingsClicked: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Notification Permission
    // TODO fix this isn't the way it was meant to be implemented and if notification perms are revoked the app just breaks
    val notificationState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    if (notificationState.status.isGranted) {
        HomeScreen(
            state = state,
            callbacks = viewModel.callbacks,
            onSettingsClicked = onSettingsClicked
        )
    }
}

@Composable
fun HomeScreen(
    state: HomeScreenState,
    callbacks: HomeScreenCallbacks,
    onSettingsClicked: () -> Unit = {}
) {
    when(state) {
        HomeScreenState.Loading -> CircularProgressIndicator()
        is HomeScreenState.Success -> HomeScreenInner(
            state,
            callbacks,
            onSettingsClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenInner(
    state: HomeScreenState.Success,
    callbacks: HomeScreenCallbacks,
    onSettingsClicked: () -> Unit = {}
) {
    val launcher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("nsp", "xci")),
        mode = PickerMode.Single,
        title = "Pick a rom",
        onResult = { it?.let(callbacks::onFileUriSelected) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.selectedFileNum == 0) {
                            val (protoIcon, protoStr) = when (state.activeProtocol) {
                                Protocol.GoldLeafUSB -> Pair(Icons.Outlined.Usb, R.string.gl)
                                Protocol.Tinfoil.NET -> Pair(Icons.Outlined.Wifi, R.string.tf_net)
                                Protocol.Tinfoil.USB -> Pair(Icons.Outlined.Usb, R.string.tf_usb)
                            }
                            Icon(imageVector = protoIcon, contentDescription = null)
                            Text(stringResource(protoStr))
                        } else {
                            Icon(
                                modifier = Modifier.clickable { callbacks.onClearSelected() },
                                imageVector = Icons.Outlined.Cancel, contentDescription = null)
                            Text("${state.selectedFileNum} Selected")
                        }
                    }
                },
                actions = {
                    if (state.selectedFileNum == 0) {
                        IconButton(
                            onClick = launcher::launch
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.NoteAdd,
                                contentDescription = "Select files icon"
                            )
                        }
                        IconButton(
                            onClick = onSettingsClicked
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings icon"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = callbacks::onUploadFileClicked
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_upload_btn),
                                contentDescription = "Upload icon"
                            )
                        }
                        IconButton(
                            onClick = callbacks::onDeleteSelected
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Trash Icon"
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        // Screen content
        LazyColumn(
            modifier = Modifier.padding(contentPadding).fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.files) { file ->
                HomeFileCard(modifier = Modifier.fillMaxWidth(), file = file, onFileClicked = callbacks::onFileClicked)
            }
        }
    }
}

@ThemePreviews
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            state = HomeScreenState.Success(
                activeProtocol = Protocol.Tinfoil.USB,
                files = emptyList(),
                selectedFileNum = 1
            ),
            callbacks = HomeScreenCallbacks.Default
        )
    }
}
