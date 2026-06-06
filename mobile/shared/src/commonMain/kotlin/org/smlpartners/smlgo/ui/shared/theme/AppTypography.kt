package org.smlpartners.smlgo.ui.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SMLGoTypography = Typography(
    // Títulos grandes — pantallas principales
    headlineLarge = TextStyle(
        fontSize   = 32.sp,
        fontWeight = FontWeight.Bold,
        color      = TextPrimary,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontSize   = 24.sp,
        fontWeight = FontWeight.Bold,
        color      = TextPrimary,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontSize   = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextPrimary,
        lineHeight = 28.sp
    ),
    // Títulos de sección
    titleLarge = TextStyle(
        fontSize   = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextPrimary
    ),
    titleMedium = TextStyle(
        fontSize   = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextPrimary
    ),
    titleSmall = TextStyle(
        fontSize   = 14.sp,
        fontWeight = FontWeight.Medium,
        color      = TextPrimary
    ),
    // Cuerpo de texto
    bodyLarge = TextStyle(
        fontSize   = 16.sp,
        fontWeight = FontWeight.Normal,
        color      = TextSecondary,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize   = 14.sp,
        fontWeight = FontWeight.Normal,
        color      = TextSecondary,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize   = 12.sp,
        fontWeight = FontWeight.Normal,
        color      = TextMuted,
        lineHeight = 16.sp
    ),
    // Labels
    labelLarge = TextStyle(
        fontSize   = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextInverse
    ),
    labelMedium = TextStyle(
        fontSize   = 12.sp,
        fontWeight = FontWeight.Medium,
        color      = TextSecondary
    ),
    labelSmall = TextStyle(
        fontSize   = 11.sp,
        fontWeight = FontWeight.Medium,
        color      = TextMuted
    )
)