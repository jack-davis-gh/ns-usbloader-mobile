package com.blogspot.developersu.ns_usbloader

import android.content.res.Resources.Theme
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    MainScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainScreenDrawer()
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
            }
        ) { contentPadding ->
            // Screen content
            Box(modifier = Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun MainScreenDrawer() {
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

            DrawerIconText(R.drawable.ic_usb, "USB Icon", R.string.tf_usb, true)
            DrawerIconText(R.drawable.ic_net, "Wifi Icon", R.string.tf_net)
            DrawerIconText(R.drawable.ic_usb, "USB Icon", R.string.gl)

            Text(stringResource(R.string.other))

            DrawerIconText(R.drawable.ic_settings, "Gear Icon", R.string.settings)
            DrawerIconText(R.drawable.ic_info, "Info Icon", R.string.about_app)
        }
    }
}

@Composable
private fun DrawerIconText(
    @DrawableRes drawbleId: Int,
    contentDescription: String,
    @StringRes textId: Int,
    active: Boolean = false
) {
    val modifier = if (active) {
        Modifier.fillMaxWidth()
            .background(colorResource(R.color.colorPrimary), RoundedCornerShape(5.dp))
            .padding(10.dp)
    } else {
        Modifier.fillMaxWidth()
            .padding(10.dp)
    }
    val color = if (active) { Color.White } else { Color.Black }

    Row(
        modifier = modifier,
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

@Preview
@Composable
private fun MainScreenDrawerPreview() {
    MainScreenDrawer()
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}
