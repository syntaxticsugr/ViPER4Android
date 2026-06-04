package com.llsl.viper4android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography. This is the stock Material 3 type scale and matches the reference app, which draws
 * everything in the platform `sans-serif`. (A Google Sans-style font is only an optional,
 * user-selectable choice there, never the default, so we don't bundle one.)
 *
 * A couple of titles are rendered bold on top of this scale rather than baked in here: the large
 * toolbar title is headlineSmall bold, the regular toolbar title is titleLarge bold, and the hero
 * "Enabled/Disabled" label is titleMedium bold. Apply [FontWeight.Bold] at those call sites.
 */
private val Sans = FontFamily.SansSerif

val ViperTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = Sans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
    )
