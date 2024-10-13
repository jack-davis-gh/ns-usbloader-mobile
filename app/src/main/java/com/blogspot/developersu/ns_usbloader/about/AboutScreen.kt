package com.blogspot.developersu.ns_usbloader.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blogspot.developersu.ns_usbloader.R

sealed interface DonateUiClick {
    data object Liberapay: DonateUiClick
    data object Paypal: DonateUiClick
    data object Yandex: DonateUiClick
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    onBackPressed: () -> Unit,
    onDonatePressed: (DonateUiClick) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("About application") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPressed,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
        }
    ) {
        Column(
            modifier = modifier.padding(it).padding(10.dp)
        ) {
            Image(
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(start = 10.dp, end = 10.dp).align(Alignment.CenterHorizontally),
                painter = painterResource(R.drawable.ic_fullsize_logo_android),
                contentDescription = "application logo"
            )

            Text(
                modifier = Modifier
//                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_1)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body2"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_2)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_3)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_4)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_5)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp, start = 5.dp),
                text = stringResource(R.string.translators)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_contributors)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.contributors)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 5.dp),
                text = stringResource(R.string.about_line_6)
                // TODO textAppearance="@style/TextAppearance.AppCompat.Body1"
            )

            Image(
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
                    .clickable { onDonatePressed(DonateUiClick.Liberapay) },
                painter = painterResource(R.drawable.ic_donate_libera),
                contentDescription = "Donate Liberapay"
            )

            Image(
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
                    .clickable { onDonatePressed(DonateUiClick.Paypal) },
                painter = painterResource(R.drawable.ic_donate_paypal),
                contentDescription = "Donate Paypal"
            )

            Image(
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
                    .clickable { onDonatePressed(DonateUiClick.Yandex) },
                painter = painterResource(R.drawable.ic_donate_yandex),
                contentDescription = "Donate Yandex Money"
            )
        }
    }
}