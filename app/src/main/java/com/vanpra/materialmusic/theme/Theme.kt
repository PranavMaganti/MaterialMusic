package com.vanpra.materialmusic.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MaterialMusicTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        dynamicLightColorScheme(LocalContext.current)
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}