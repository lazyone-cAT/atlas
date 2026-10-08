package com.atlas.bookwriter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Design System typography:
// Display / headings / labels / buttons -> handwriting feel (OS cursive family)
// Body / reading                       -> quiet legible serif (or the user's chosen font)
// Scale 1.25, base body 18sp, body line-height 1.6, display line-height 1.2.

val DisplaySerif = FontFamily.Cursive
val BodySerif = FontFamily.Serif
val UiSans = FontFamily.SansSerif

fun atlasTypography(
    fontFamily: FontFamily = FontFamily.Default,
    fontSize: Int = 16,
    textColor: androidx.compose.ui.graphics.Color = Ink,
    dimColor: androidx.compose.ui.graphics.Color = Ink
): Typography {
    // Respect user font-size setting for body; display follows 1.25 modular scale.
    val base = 18f
    val userScale = fontSize / 18f
    val bodySize = (base * userScale)
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Bold,
            fontSize = (bodySize * 1.25 * 1.25 * 1.25).sp,
            lineHeight = (bodySize * 1.25 * 1.25 * 1.25 * 1.2f).sp,
            color = textColor
        ),
        headlineMedium = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (bodySize * 1.25 * 1.25).sp,
            lineHeight = (bodySize * 1.25 * 1.25 * 1.2f).sp,
            color = textColor
        ),
        headlineSmall = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (bodySize * 1.25).sp,
            lineHeight = (bodySize * 1.25 * 1.2f).sp,
            color = textColor
        ),
        titleLarge = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (bodySize * 1.25).sp,
            lineHeight = (bodySize * 1.25 * 1.2f).sp,
            color = textColor
        ),
        titleMedium = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = textColor
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = bodySize.sp,
            lineHeight = (bodySize * 1.6f).sp,
            color = textColor
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = (16 * 1.6f).sp,
            color = dimColor
        ),
        bodySmall = TextStyle(
            fontFamily = UiSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = dimColor
        ),
        labelLarge = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = textColor
        ),
        labelMedium = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = dimColor
        ),
        labelSmall = TextStyle(
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
            color = dimColor
        )
    )
}

val AtlasTypography = atlasTypography()
