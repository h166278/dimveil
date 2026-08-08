package com.h166278.dimveil.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Apple iOS dark system palette
private val AppleBlack = Color(0xFF000000)          // systemBackground
private val AppleCard = Color(0xFF1C1C1E)           // secondarySystemGroupedBackground
private val AppleFill = Color(0xFF2C2C2E)           // secondarySystemFill
private val AppleSeparator = Color(0xFF38383A)
private val AppleLabel = Color(0xFFFFFFFF)
private val AppleSecondary = Color(0xFF98989F)
private val AppleBlue = Color(0xFF0A84FF)           // systemBlue
private val AppleGreen = Color(0xFF30D158)          // systemGreen
private val AppleRed = Color(0xFFFF453A)            // systemRed

private val DimVeilColors = darkColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B3A5C),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = AppleGreen,
    onSecondary = Color(0xFF04281A),
    secondaryContainer = Color(0xFF1E3A2F),
    onSecondaryContainer = Color(0xFFC7F0DC),
    background = AppleBlack,
    onBackground = AppleLabel,
    surface = AppleCard,
    onSurface = AppleLabel,
    surfaceVariant = AppleFill,
    onSurfaceVariant = AppleSecondary,
    surfaceContainerLowest = AppleBlack,
    surfaceContainerLow = Color(0xFF141416),
    surfaceContainer = AppleCard,
    surfaceContainerHigh = Color(0xFF262628),
    surfaceContainerHighest = AppleFill,
    outline = AppleSeparator,
    outlineVariant = Color(0xFF2A2A2C),
    error = AppleRed,
    onError = Color.White,
    inverseSurface = AppleLabel,
    inverseOnSurface = AppleBlack,
    scrim = AppleBlack
)

private val DimVeilShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun DimVeilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DimVeilColors, shapes = DimVeilShapes, content = content)
}
