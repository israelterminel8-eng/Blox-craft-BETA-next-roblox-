package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BloxGamerColorScheme = darkColorScheme(
  primary = BloxCyanPrimary,
  onPrimary = Color(0xFF002026),
  primaryContainer = Color(0xFF004D59),
  onPrimaryContainer = Color(0xFF80F3FF),
  secondary = BloxPurpleAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF4C1D95),
  onSecondaryContainer = Color(0xFFDDD6FE),
  tertiary = BloxGoldCoins,
  onTertiary = Color(0xFF3B2D00),
  tertiaryContainer = Color(0xFF5C4600),
  onTertiaryContainer = Color(0xFFFFDF7A),
  background = BloxDarkBackground,
  onBackground = TextPrimary,
  surface = BloxDarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = BloxDarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  error = BloxLavaRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = BloxGamerColorScheme,
    typography = Typography,
    content = content
  )
}
