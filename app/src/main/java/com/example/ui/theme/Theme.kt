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

private val DarkColorScheme =
  darkColorScheme(
    primary = GreenPastel,
    onPrimary = TextDark,
    primaryContainer = GreenDark,
    onPrimaryContainer = Color.White,
    secondary = LemonSoft,
    onSecondary = TextDark,
    background = Color(0xFF131D16),
    onBackground = Color(0xFFE8F0EA),
    surface = Color(0xFF1B2E21),
    onSurface = Color(0xFFE8F0EA),
    surfaceVariant = Color(0xFF283F2F),
    onSurfaceVariant = Color(0xFFA8B4AB),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GreenDark,
    onPrimary = Color.White,
    primaryContainer = GreenPastel,
    onPrimaryContainer = TextDark,
    secondary = LemonSoft,
    onSecondary = TextDark,
    background = CreamBg,
    onBackground = TextDark,
    surface = CreamSurface,
    onSurface = TextDark,
    surfaceVariant = LightGray,
    onSurfaceVariant = NeutralGray,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
