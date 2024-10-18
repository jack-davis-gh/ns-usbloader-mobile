package com.blogspot.developersu.ns_usbloader.home

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.model.NSFile
import com.blogspot.developersu.ns_usbloader.model.Protocol
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.launch

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onSettingsClicked: () -> Unit,
    onAboutClicked: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val files by viewModel.files.collectAsStateWithLifecycle()
    // Notification Permission
    // TODO fix this isn't the way it was meant to be implemented and if notification perms are revoked the app just breaks
    val notificationState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    if (notificationState.status.isGranted) {
        HomeScreen(
            activeProtocol = viewModel.activeProtocol,
            files = files,
            onFileUriSelected = viewModel::onFileUriSelected,
            onProtocolChanged = viewModel::updateActiveProtocol,
            onUploadFileClicked = viewModel::uploadFile,
            onSettingsClicked = onSettingsClicked,
            onFileClicked = viewModel::onClickFile,
            onAboutClicked = onAboutClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeProtocol: Protocol,
    files: List<NSFile>,
    onProtocolChanged: (Protocol) -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onAboutClicked: () -> Unit = {},
    onFileClicked: (NSFile) -> Unit = {},
    onUploadFileClicked: () -> Unit = {},
    onFileUriSelected: (PlatformFile) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun drawerClose() = scope.launch { drawerState.close() }

    val launcher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("nsp", "xci")),
        mode = PickerMode.Single,
        title = "Pick a rom",
        onResult = { it?.let(onFileUriSelected) }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeScreenDrawer(activeProtocol = activeProtocol,
                onProtocolChanged = { onProtocolChanged(it).also { drawerClose() } },
                onSettingsClicked = { onSettingsClicked().also { drawerClose() } },
                onAboutClicked = { onAboutClicked().also { drawerClose() } }
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        @StringRes val protoStr: Int = when (activeProtocol) {
                            Protocol.GoldLeafUSB -> R.string.gl
                            Protocol.Tinfoil.NET -> R.string.tf_net
                            Protocol.Tinfoil.USB -> R.string.tf_usb
                        }
                        Text(stringResource(protoStr))
                    },
                    actions = {
                        IconButton(
                            onClick = launcher::launch
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.NoteAdd,
                                contentDescription = "Select files icon"
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) { Icon(Icons.Filled.Menu, contentDescription = "Open menu button") }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onUploadFileClicked
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_upload_btn),
                        contentDescription = "Upload files to NS Icon"
                    )
                }
            }
        ) { contentPadding ->
            // Screen content
            LazyColumn(
                modifier = Modifier.padding(contentPadding).fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(files) { file ->
                    HomeFileUi(modifier = Modifier.fillMaxWidth(), file = file, onFileClicked = onFileClicked)
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            activeProtocol = Protocol.Tinfoil.USB,
            files = emptyList()
        )
    }
}
