package com.blogspot.developersu.ns_usbloader.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blogspot.developersu.ns_usbloader.core.model.Protocol
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews

@Composable
fun HomeScreenDrawer(
    activeProtocol: Protocol,
    onProtocolChanged: (Protocol) -> Unit,
    onSettingsClicked: () -> Unit = {},
    onAboutClicked: () -> Unit = {}
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

            DrawerIconText(R.drawable.ic_usb, "USB Icon", R.string.tf_usb, activeProtocol == Protocol.Tinfoil.USB, onClick = { onProtocolChanged(
                Protocol.Tinfoil.USB) })
            DrawerIconText(R.drawable.ic_net, "Wifi Icon", R.string.tf_net, activeProtocol == Protocol.Tinfoil.NET, onClick = { onProtocolChanged(
                Protocol.Tinfoil.NET) })
            DrawerIconText(R.drawable.ic_usb, "USB Icon", R.string.gl, activeProtocol == Protocol.GoldLeafUSB, onClick = { onProtocolChanged(
                Protocol.GoldLeafUSB) })

            Text(stringResource(R.string.other))

            DrawerIconText(R.drawable.ic_settings, "Gear Icon", R.string.settings, onClick = onSettingsClicked)
            DrawerIconText(R.drawable.ic_info, "Info Icon", R.string.about_app, onClick = onAboutClicked)
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
            activeProtocol = Protocol.Tinfoil.USB,
            onProtocolChanged = {}
        )
    }
}