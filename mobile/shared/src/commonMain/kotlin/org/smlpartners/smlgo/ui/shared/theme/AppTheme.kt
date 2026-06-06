package org.smlpartners.smlgo.ui.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

// ── Schemes ───────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = TextInverse,
    primaryContainer   = Color(0xFFFFE5B4),
    onPrimaryContainer = Color(0xFF3D2400),
    secondary          = Secondary,
    onSecondary        = TextInverse,
    secondaryContainer = Color(0xFFFFF0D4),
    onSecondaryContainer = TextPrimary,
    tertiary           = Success,
    onTertiary         = TextInverse,
    background         = Background,
    onBackground       = TextPrimary,
    surface            = Surface,
    onSurface          = TextPrimary,
    surfaceVariant     = Color(0xFFFFF0D4),
    onSurfaceVariant   = TextSecondary,
    error              = Error,
    onError            = TextInverse,
    outline            = Color(0xFFE2E8F0),
    outlineVariant     = Color(0xFFF1F5F9)
)

private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryDark,
    onPrimary          = Color(0xFF3D2400),
    primaryContainer   = Color(0xFF5C3600),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary          = Secondary,
    onSecondary        = Color(0xFF3D2400),
    secondaryContainer = Color(0xFF5C3600),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary           = Success,
    onTertiary         = TextInverse,
    background         = BackgroundDark,
    onBackground       = TextPrimaryDark,
    surface            = SurfaceDark,
    onSurface          = TextPrimaryDark,
    surfaceVariant     = Color(0xFF3D2400),
    onSurfaceVariant   = Color(0xFFCBD5E1),
    error              = Color(0xFFFF6B6B),
    onError            = Color(0xFF3D0000),
    outline            = Color(0xFF334155),
    outlineVariant     = Color(0xFF1E293B)
)

// ── Shapes ────────────────────────────────────────────────────────────────

private val SMLGoShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.sm),
    small      = RoundedCornerShape(Radius.sm),
    medium     = RoundedCornerShape(Radius.md),
    large      = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl)
)

// ── CompositionLocal para acceder a colores custom ────────────────────────

data class SMLGoColors(
    val glass    : Color,
    val glassDark: Color,
    val success  : Color,
    val accent   : Color
)

val LocalSMLGoColors = staticCompositionLocalOf {
    SMLGoColors(
        glass     = Glass,
        glassDark = GlassDark,
        success   = Success,
        accent    = Accent
    )
}

// Acceso rápido desde cualquier Composable
// Uso: SMLGoTheme.colors.glass
object SMLGoTheme {
    val colors: SMLGoColors
        @Composable get() = LocalSMLGoColors.current
}

// ── Theme principal ───────────────────────────────────────────────────────

@Composable
fun SMLGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalSMLGoColors provides SMLGoColors(
            glass     = Glass,
            glassDark = GlassDark,
            success   = Success,
            accent    = Accent
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = SMLGoTypography,
            shapes      = SMLGoShapes,
            content     = content
        )
    }
}