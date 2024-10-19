package com.blogspot.developersu.ns_usbloader.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTypography
import com.blogspot.developersu.ns_usbloader.ui.theme.ThemePreviews

@Composable
fun HomeFileCard(
    modifier: Modifier = Modifier,
    file: NSFile,
    onFileClicked: (NSFile) -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onFileClicked(file) }
    ) {
        Row(modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically) {

            if (file.isSelected) {
                Checkbox(
                    modifier = Modifier.size(height = 20.dp, width = 30.dp)
                        .padding(end = 10.dp),
                    checked = true,
                    onCheckedChange = {}
                )
            }
            
            Column {
                Text(text = file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = file.cuteSize, style = AppTypography.bodySmall, fontWeight = FontWeight.Thin)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun HomeFileCardPreview() {
    HomeFileCard(
        file = NSFile(
            uri = "",
            name = "SomeBackup.nsp",
            size = 2_000_000_000L,
            isSelected = true
        )
    )
}