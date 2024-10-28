package com.github.jack_davis_gh.ns_usbloader.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jack_davis_gh.ns_usbloader.R
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.home.ui.HomeFileCard
import com.github.jack_davis_gh.ns_usbloader.ui.theme.AppTheme
import com.github.jack_davis_gh.ns_usbloader.ui.theme.ThemePreviews
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import me.tatarka.inject.annotations.Inject

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

interface HomeComponent {
    val homeViewModel: () -> HomeViewModel
}

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Inject
@Composable
fun HomeScreen(
    homeViewModel: () -> HomeViewModel,
    onSettingsClicked: () -> Unit = {}
) {
    val viewModel = viewModel { homeViewModel() }
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
    } else {
        Scaffold { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                val textToShow = if (notificationState.status.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // then gently explain why the app requires this permission
                    "Notification perms are required for progress. Please grant the permission."
                } else {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    // TODO notification perms shouldn't be required
                    "Notification perms are required for progress. Please grant the permission."
                }
                Text(textToShow)
                Button(onClick = { notificationState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }
        }
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
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.selectedFileNum == 0) {
                            val (protoIcon, protoStr) = when (state.activeProtocol) {
                                Protocol.Network -> Pair(Icons.Outlined.Wifi, R.string.tf_net)
                                Protocol.USB -> Pair(Icons.Outlined.Usb, R.string.tf_usb)
                            }
                            Icon(imageVector = protoIcon, contentDescription = null)
                            Text(stringResource(protoStr))
                        } else {
                            IconButton(onClick = callbacks::onClearSelected) {
                                Icon(imageVector = Icons.Outlined.Cancel,
                                    contentDescription = null)
                            }

                            Text("${state.selectedFileNum} Selected")
                        }
                    }
                },
                actions = {
                    if (state.selectedFileNum == 0) {
                        IconButton(launcher::launch) {
                            Icon(Icons.AutoMirrored.Outlined.NoteAdd,
                                contentDescription = "Select files icon")
                        }
                        IconButton(onSettingsClicked) {
                            Icon(Icons.Outlined.Settings,
                                contentDescription = "Settings icon")
                        }
                    } else {
                        IconButton(callbacks::onUploadFileClicked) {
                            Icon(painterResource(R.drawable.ic_upload_btn),
                                contentDescription = "Upload icon")
                        }
                        IconButton(callbacks::onDeleteSelected) {
                            Icon(Icons.Outlined.Delete,
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
                HomeFileCard(modifier = Modifier.fillMaxWidth(), file = file,
                    onFileClicked = callbacks::onFileClicked)
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
                activeProtocol = Protocol.USB,
                files = emptyList(),
                selectedFileNum = 1
            ),
            callbacks = HomeScreenCallbacks.Default
        )
    }
}
