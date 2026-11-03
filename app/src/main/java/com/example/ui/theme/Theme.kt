package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = BrandBlueLight,
  onPrimary = Slate900,
  primaryContainer = BrandBlueDark,
  onPrimaryContainer = Color.White,
  secondary = BrandAccentAmber,
  onSecondary = Slate900,
  background = SurfaceDark,
  onBackground = Color(0xFFF1F5F9),
  surface = CardDark,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = Slate800,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
  primary = BrandBluePrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0E7FF),
  onPrimaryContainer = BrandBlueDark,
  secondary = BrandAccentAmber,
  onSecondary = Slate900,
  background = Slate50,
  onBackground = Slate900,
  surface = SurfaceLight,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent B Store branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
