package com.blogspot.developersu.ns_usbloader.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AboutScreen(
                onBackPressed = { finish() },
                onDonatePressed = ::onDonateAction
            )
        }
    }

    private fun onDonateAction(donateUiClicked: DonateUiClick) {
        val url = when (donateUiClicked) {
            DonateUiClick.Liberapay -> "https://liberapay.com/developersu/donate"
            DonateUiClick.Paypal -> "https://www.paypal.me/developersu"
            DonateUiClick.Yandex -> "https://money.yandex.ru/to/410014301951665"
        }
        // create OpenBrowserIntent
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        intent.setData(Uri.parse(url))

        startActivity(intent)
    }
}
