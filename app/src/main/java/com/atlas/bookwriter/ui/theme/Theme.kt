package com.atlas.bookwriter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.atlas.bookwriter.model.AppSettings

private val BookLightScheme = lightColorScheme(
    primary = Leather,
    onPrimary = Paper,
    secondary = LeatherDeep,
    onSecondary = Paper,
    tertiary = Tan,
    background = Paper,
    onBackground = Ink,
    surface = PaperPure,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Ink,
    outline = Tan,
    error = Rust
)

private val BookDarkScheme = darkColorScheme(
    primary = Color(0xFFC9D0DC),
    onPrimary = Color(0xFF0A0A0A),
    secondary = Color(0xFF9AA2B0),
    onSecondary = Color(0xFF0A0A0A),
    tertiary = Color(0xFF7A8290),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF141416),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF1A1A1E),
    onSurfaceVariant = Color(0xFFB0B0B8),
    outline = Color(0xFF303038),
    error = Color(0xFFD98868)
)

@Composable
fun AtlasTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (settings.isLightTheme) BookLightScheme else BookDarkScheme

    val typography = atlasTypography(
        fontFamily = settings.appFont.fontFamily,
        fontSize = settings.fontSize,
        textColor = colorScheme.onBackground,
        dimColor = colorScheme.onSurfaceVariant
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
