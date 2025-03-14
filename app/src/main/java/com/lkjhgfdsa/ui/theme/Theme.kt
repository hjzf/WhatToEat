package com.lkjhgfdsa.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lkjhgfdsa.R

data class ThemeState(
    val tips: Color = Color(0xFFF7A44C),
    val primaryTextColor: Color = Color(0xFF151515),
    val secondTextColor: Color = Color(0xFFB2B2B2),
    val card: Color = Color(0xFFFFFFFF),
    val background: Color = Color(0xFFF6F6F6),
    val deleteButtonBackground: Color = Color(0xFFFF3C3C),
    val backIcon: Int = R.drawable.ic_back_black,
    val addOptionIcon: Int = R.drawable.ic_add_black,
)

val lightThemeState = ThemeState()
val darkThemeState = ThemeState(
    primaryTextColor = Color(0xFFF6F6F6),
    secondTextColor = Color(0xFFB2B2B2),
    card = Color(0xFF151515),
    background = Color(0xFF000000),
    deleteButtonBackground = Color(0xFFD83535),
    backIcon = R.drawable.ic_back_white,
    addOptionIcon = R.drawable.ic_add_white,
)

val LocalTheme = compositionLocalOf { lightThemeState }

@Composable
fun Theme(
    isDark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit
) {
    val localTheme = remember(isDark) {
        mutableStateOf(
            if (isDark) {
                darkThemeState
            } else {
                lightThemeState
            }
        )
    }
    CompositionLocalProvider(LocalTheme provides localTheme.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = localTheme.value.background)
        ) {
            content()
        }
    }
}