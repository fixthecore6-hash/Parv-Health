package com.example.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.ThemeMode

@Immutable
data class AccentColors(
    val greenPrimary: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val greenTint: Color,
    val goalLine: Color,
    val bmiUnderweight: Color,
    val bmiHealthy: Color,
    val bmiOverweight: Color,
    val bmiObese: Color
) {
    val horizontalGradient: Brush
        get() = Brush.horizontalGradient(listOf(gradientStart, gradientEnd))

    val verticalGradient: Brush
        get() = Brush.verticalGradient(listOf(gradientEnd, gradientStart))
}

val LocalIsDarkTheme = compositionLocalOf { false }
val LocalAccentColors = compositionLocalOf {
    AccentColors(
        greenPrimary = GreenPrimary,
        gradientStart = GreenBright,
        gradientEnd = GreenPrimary,
        greenTint = GreenSoftMint,
        goalLine = PureBlack,
        bmiUnderweight = BmiZoneUnderweightLight,
        bmiHealthy = BmiZoneHealthyLight,
        bmiOverweight = BmiZoneOverweightLight,
        bmiObese = BmiZoneObeseLight
    )
}

private val LightPalette = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = PureBlack,
    primaryContainer = GreenSoftMint,
    onPrimaryContainer = PureBlack,
    secondary = GreenBright,
    onSecondary = PureBlack,
    secondaryContainer = GreenSoftMint,
    onSecondaryContainer = PureBlack,
    tertiary = GreenDeep,
    onTertiary = PureWhite,
    tertiaryContainer = LightSurfaceHigh,
    onTertiaryContainer = PureBlack,
    background = LightBackground,
    onBackground = LightInkPrimary,
    surface = LightSurfaceCard,
    onSurface = LightInkPrimary,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = LightInkSecondary,
    surfaceContainerLowest = LightBackground,
    surfaceContainerLow = LightSurfaceCard,
    surfaceContainer = LightSurfaceCard,
    surfaceContainerHigh = LightSurfaceHigh,
    outline = LightBorder,
    error = PureBlack,
    onError = PureWhite
)

private val DarkPalette = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = PureBlack,
    primaryContainer = GreenDarkTint,
    onPrimaryContainer = PureWhite,
    secondary = GreenDeep,
    onSecondary = PureWhite,
    secondaryContainer = GreenDarkTint,
    onSecondaryContainer = PureWhite,
    tertiary = GreenBright,
    onTertiary = PureBlack,
    tertiaryContainer = DarkSurfaceHigh,
    onTertiaryContainer = PureWhite,
    background = DarkBackground,
    onBackground = DarkInkPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkInkPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkInkSecondary,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurfaceCard,
    surfaceContainer = DarkSurfaceCard,
    surfaceContainerHigh = DarkSurfaceHigh,
    outline = DarkBorder,
    error = PureWhite,
    onError = PureBlack
)

private val ParvHealthShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
private fun animatedColor(target: Color, label: String): Color {
    val color by animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 280),
        label = label
    )
    return color
}

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme {
    return target.copy(
        primary = animatedColor(target.primary, "primary"),
        onPrimary = animatedColor(target.onPrimary, "onPrimary"),
        primaryContainer = animatedColor(target.primaryContainer, "primaryContainer"),
        onPrimaryContainer = animatedColor(target.onPrimaryContainer, "onPrimaryContainer"),
        secondary = animatedColor(target.secondary, "secondary"),
        onSecondary = animatedColor(target.onSecondary, "onSecondary"),
        secondaryContainer = animatedColor(target.secondaryContainer, "secondaryContainer"),
        onSecondaryContainer = animatedColor(target.onSecondaryContainer, "onSecondaryContainer"),
        tertiary = animatedColor(target.tertiary, "tertiary"),
        onTertiary = animatedColor(target.onTertiary, "onTertiary"),
        tertiaryContainer = animatedColor(target.tertiaryContainer, "tertiaryContainer"),
        onTertiaryContainer = animatedColor(target.onTertiaryContainer, "onTertiaryContainer"),
        background = animatedColor(target.background, "background"),
        onBackground = animatedColor(target.onBackground, "onBackground"),
        surface = animatedColor(target.surface, "surface"),
        onSurface = animatedColor(target.onSurface, "onSurface"),
        surfaceVariant = animatedColor(target.surfaceVariant, "surfaceVariant"),
        onSurfaceVariant = animatedColor(target.onSurfaceVariant, "onSurfaceVariant"),
        surfaceContainerLowest = animatedColor(target.surfaceContainerLowest, "surfaceContainerLowest"),
        surfaceContainerLow = animatedColor(target.surfaceContainerLow, "surfaceContainerLow"),
        surfaceContainer = animatedColor(target.surfaceContainer, "surfaceContainer"),
        surfaceContainerHigh = animatedColor(target.surfaceContainerHigh, "surfaceContainerHigh"),
        outline = animatedColor(target.outline, "outline"),
        error = animatedColor(target.error, "error"),
        onError = animatedColor(target.onError, "onError")
    )
}

@Composable
fun ParvHealthTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.BRIGHT -> false
        ThemeMode.DARK -> true
    }

    val animatedColors = animateColorScheme(if (isDark) DarkPalette else LightPalette)
    val accentColors = AccentColors(
        greenPrimary = animatedColor(GreenPrimary, "greenPrimary"),
        gradientStart = animatedColor(if (isDark) GreenDeep else GreenBright, "gradStart"),
        gradientEnd = animatedColor(GreenPrimary, "gradEnd"),
        greenTint = animatedColor(if (isDark) GreenDarkTint else GreenSoftMint, "greenTint"),
        goalLine = animatedColor(if (isDark) PureWhite else PureBlack, "goalLine"),
        bmiUnderweight = animatedColor(if (isDark) BmiZoneUnderweightDark else BmiZoneUnderweightLight, "bmiUnder"),
        bmiHealthy = animatedColor(if (isDark) BmiZoneHealthyDark else BmiZoneHealthyLight, "bmiHealthy"),
        bmiOverweight = animatedColor(if (isDark) BmiZoneOverweightDark else BmiZoneOverweightLight, "bmiOver"),
        bmiObese = animatedColor(if (isDark) BmiZoneObeseDark else BmiZoneObeseLight, "bmiObese")
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
        LocalAccentColors provides accentColors
    ) {
        MaterialTheme(
            colorScheme = animatedColors,
            typography = ParvHealthTypography,
            shapes = ParvHealthShapes,
            content = content
        )
    }
}
