package com.h166278.dimveil.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DimVeilColors = darkColorScheme(
    primary = Color(0xFFC8A4FF),
    onPrimary = Color(0xFF24103D),
    primaryContainer = Color(0xFF382461),
    onPrimaryContainer = Color(0xFFE8DCFF),
    secondary = Color(0xFF8ADCC9),
    onSecondary = Color(0xFF09251F),
    secondaryContainer = Color(0xFF12362F),
    onSecondaryContainer = Color(0xFFC5F7E9),
    tertiary = Color(0xFFE9BE6A),
    onTertiary = Color(0xFF2A1F08),
    background = Color(0xFF090917),
    onBackground = Color(0xFFF4F0FF),
    surface = Color(0xFF111223),
    onSurface = Color(0xFFF4F0FF),
    surfaceVariant = Color(0xFF1B1A31),
    onSurfaceVariant = Color(0xFFBBB6D1),
    surfaceContainerLowest = Color(0xFF070711),
    surfaceContainerLow = Color(0xFF0D0D1B),
    surfaceContainer = Color(0xFF111223),
    surfaceContainerHigh = Color(0xFF19182C),
    surfaceContainerHighest = Color(0xFF211E38),
    outline = Color(0xFF45405C),
    outlineVariant = Color(0xFF28263E),
    error = Color(0xFFE57373),
    onError = Color(0xFF2B0A0A),
    inverseSurface = Color(0xFFF4F0FF),
    inverseOnSurface = Color(0xFF111223),
    scrim = Color(0xFF000000)
)

private val DimVeilShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun DimVeilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DimVeilColors, shapes = DimVeilShapes, content = content)
}
