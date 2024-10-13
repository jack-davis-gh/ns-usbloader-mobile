package com.blogspot.developersu.ns_usbloader

import androidx.appcompat.app.AppCompatDelegate

object ApplicationTheme {
    private const val SYSTEM_DEFAULT = 0
    private const val DAY_THEME = 1
    private const val NIGHT_THEME = 2

    fun setApplicationTheme(itemId: Int) {
        when (itemId) {
            SYSTEM_DEFAULT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            DAY_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            NIGHT_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
