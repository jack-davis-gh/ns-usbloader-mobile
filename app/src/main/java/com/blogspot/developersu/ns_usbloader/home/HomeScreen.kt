package com.blogspot.developersu.ns_usbloader.home

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.model.NSFile
import com.blogspot.developersu.ns_usbloader.model.Protocol
import com.blogspot.developersu.ns_usbloader.model.asNSFile
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onSettingsClicked: () -> Unit,
    onAboutClicked: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // Notification Permission
    val notificationState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    if (notificationState.status.isGranted) {
        HomeScreen(
            activeProtocol = viewModel.activeProtocol,
            files = viewModel.files,
            selectFileUri = viewModel::selectFileUri,
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
    selectFileUri: (NSFile) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun drawerClose() = scope.launch { drawerState.close() }

    val launcher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("nsp", "xci")),
        mode = PickerMode.Single,
        title = "Pick a rom"
    ) { files ->
        files?.asNSFile()?.let(selectFileUri)
    }

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
                    title = { Text("NS Usb Loader") },
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
            }
        ) { contentPadding ->
            // Screen content
            Column(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files) { file ->
                        HomeFileUi(modifier = Modifier.fillMaxWidth(), file = file, onFileClicked = onFileClicked)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    HomeImageButton(
                        modifier = Modifier.weight(0.5f),
                        imageRes = R.drawable.ic_upload_btn,
                        textRes = R.string.upload_btn,
                        contentDescription = "Upload files Icon",
                        onClicked = onUploadFileClicked
                    )

                    HomeImageButton(
                        modifier = Modifier.weight(0.5f),
                        imageRes = R.drawable.ic_select_file,
                        textRes = R.string.select_file_btn,
                        contentDescription = "Select files Icon",
                        onClicked = launcher::launch
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeImageButton(
    modifier: Modifier = Modifier,
    contentDescription: String,
    @DrawableRes imageRes: Int,
    @StringRes textRes: Int,
    onClicked: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClicked
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = contentDescription
            )
            Text(
                text = stringResource(textRes),
                color = Color.White
            )
        }
    }
}

@ThemePreviews
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            activeProtocol = Protocol.Tinfoil.USB,
            files = emptyList<NSFile>()
        )
    }
}
