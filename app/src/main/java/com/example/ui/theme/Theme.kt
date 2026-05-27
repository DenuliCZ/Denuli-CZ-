package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFCC66FF), // Neon purple
    secondary = Color(0xFF00FFCC), // Neon teal
    tertiary = Color(0xFFD4AF37), // Zlato-stříbrná / Gold shadow
    background = Color(0xFF121214), // Mystický soumrak base
    surface = Color(0xFF18181C),
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFFE5A9AC), // Růžovo-zlatý / Rose-gold accent
    secondary = Color(0xFF8F63F4), // Premium Indigo
    tertiary = Color(0xFF53318F),
    background = Color(0xFFF7F4EF), // Elegantní krémová
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF121214), // Dark silhouettes for text
    onSurface = Color(0xFF121214),
    onPrimary = Color.White,
    onSecondary = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Immersive premium dark studio theme is highly recommended
  dynamicColor: Boolean = false, // Disable dynamic colors to protect neon branding from wallpaper overrides
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme // Exclusively use our custom midnight studio colors
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
