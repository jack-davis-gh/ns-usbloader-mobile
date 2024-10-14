package com.blogspot.developersu.ns_usbloader.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blogspot.developersu.ns_usbloader.MainActivity
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.model.Protocol
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    HomeScreen(
        activeProtocol = viewModel.activeProtocol,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAbout = onNavigateToAbout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeProtocol: Protocol,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("nsp", "xci")),
        mode = PickerMode.Single,
        title = "Pick a rom"
    ) { files ->
        // Handle the picked files
        scope.launch {
            snackbarHostState.showSnackbar("${files?.uri}")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeScreenDrawer(activeProtocol = activeProtocol, onNavigateToSettings = {
                    onNavigateToSettings()
                    scope.launch { drawerState.close() }
                },
                onNavigateToAbout = {
                    onNavigateToAbout()
                    scope.launch { drawerState.close() }
                }
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
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open menu button")
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { contentPadding ->
            // Screen content
            Box(modifier = Modifier.padding(contentPadding)) {
                Button(
                    onClick = launcher::launch) {
                    Text(text = "Show file chooser")
                }
            }
        }
    }
}

@Composable
private fun HomeScreenDrawer(
    activeProtocol: Protocol,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxSize().padding(end = 80.dp)
    ) {
        val bgBrush = Brush.linearGradient(listOf(Color(0xFF904DFA), Color(0xFF087BB9), Color(0xFFAA62C8)))
        Image(
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(1.8f)
                .background(bgBrush),
            painter = painterResource(R.drawable.ic_game_pattern),
            contentDescription = "Drawer header decor image"
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.transfer_protocol))

            DrawerIconText(R.drawable.ic_usb, "USB Icon", R.string.tf_usb, activeProtocol == Protocol.Tinfoil.USB)
            DrawerIconText(R.drawable.ic_net, "Wifi Icon", R.string.tf_net, activeProtocol == Protocol.Tinfoil.NET)
            DrawerIconText(R.drawable.ic_usb, "USB Icon", R.string.gl, activeProtocol == Protocol.GoldLeafUSB)

            Text(stringResource(R.string.other))

            DrawerIconText(R.drawable.ic_settings, "Gear Icon", R.string.settings, onClick = onNavigateToSettings)
            DrawerIconText(R.drawable.ic_info, "Info Icon", R.string.about_app, onClick = onNavigateToAbout)
        }
    }
}

@Composable
private fun DrawerIconText(
    @DrawableRes drawbleId: Int,
    contentDescription: String,
    @StringRes textId: Int,
    active: Boolean = false,
    onClick: () -> Unit = {}
) {
    val updatedModifier = if (active) {
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp))
            .padding(10.dp)
    } else {
        Modifier.fillMaxWidth()
            .padding(10.dp)
    }
    val color = if (active) { Color.White } else { MaterialTheme.colorScheme.onBackground }

    Row(
        modifier = updatedModifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 10.dp),
            painter = painterResource(drawbleId),
            contentDescription = contentDescription,
            tint = color
        )
        Text(stringResource(textId), color = color)
    }
}

@ThemePreviews
@Composable
private fun MainScreenDrawerPreview() {
    AppTheme {
        HomeScreenDrawer(
            activeProtocol = Protocol.Tinfoil.USB
        )
    }
}

@ThemePreviews
@Composable
fun MainScreenPreview() {
    AppTheme {
        HomeScreen(
            activeProtocol = Protocol.Tinfoil.USB
        )
    }
}
