package com.atlas.bookwriter.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

enum class BackgroundTheme(
    val label: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val surface: Color
) {
    INK("Ink Night", Color(0xFF12162A), Color(0xFF191F3A), Color(0xFF212948), Color(0xFF323A5E)),
    MIDNIGHT("Midnight Blue", Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF253545), Color(0xFF344555)),
    FOREST("Deep Forest", Color(0xFF0F1A12), Color(0xFF162218), Color(0xFF1E2D20), Color(0xFF2A3D2C)),
    WARM("Warm Ember", Color(0xFF1A1210), Color(0xFF241A16), Color(0xFF2E221C), Color(0xFF3D2E26)),
    AMOLED("Pure Black", Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF141414), Color(0xFF1E1E1E)),
    SAKURA("Sakura", Color(0xFF1A1018), Color(0xFF24161E), Color(0xFF2E1C26), Color(0xFF3D2634)),
    OCEAN("Deep Ocean", Color(0xFF0A1628), Color(0xFF101E32), Color(0xFF16263C), Color(0xFF1E3048)),
    PARCHMENT("Light Parchment", Color(0xFFF5F0E8), Color(0xFFEDE5D8), Color(0xFFE0D8CA), Color(0xFFD4CCBE))
}

enum class AppFont(
    val label: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight = FontWeight.Normal
) {
    DEFAULT("System Default", FontFamily.Default),
    SERIF("Serif", FontFamily.Serif),
    MONO("Monospace", FontFamily.Monospace),
    CURSIVE("Cursive", FontFamily.Cursive),
    SANS("Sans Serif", FontFamily.SansSerif)
}

data class AppSettings(
    val backgroundTheme: BackgroundTheme = BackgroundTheme.INK,
    val appFont: AppFont = AppFont.CURSIVE,
    val fontSize: Int = 18,
    val profilePicturePath: String = "",
    val editorBackgroundPath: String = "",
    val sketchBorders: Boolean = true
) {
    val isLightTheme: Boolean get() = backgroundTheme == BackgroundTheme.PARCHMENT

    val textColor: Color get() = if (isLightTheme) Color(0xFF1B1410) else Color(0xFFFAF7F2)
    val textColorDim: Color get() = if (isLightTheme) Color(0xFF7A6A58) else Color(0xFFC9A77C)
    val accentColor: Color get() = Color(0xFF6B4226)
}
