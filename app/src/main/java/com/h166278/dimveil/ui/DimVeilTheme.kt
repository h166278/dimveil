package com.h166278.dimveil.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DimVeilColors = darkColorScheme(
    primary = Color(0xFF8BE8C1),
    onPrimary = Color(0xFF082018),
    primaryContainer = Color(0xFF1D3C35),
    onPrimaryContainer = Color(0xFFB7FFE1),
    secondary = Color(0xFF82AFA0),
    onSecondary = Color(0xFF0C1F1A),
    secondaryContainer = Color(0xFF1A2E29),
    onSecondaryContainer = Color(0xFFC4E8DC),
    tertiary = Color(0xFFE9BE6A),
    onTertiary = Color(0xFF2A1F08),
    background = Color(0xFF070B0D),
    onBackground = Color(0xFFE2ECE8),
    surface = Color(0xFF101719),
    onSurface = Color(0xFFE2ECE8),
    surfaceVariant = Color(0xFF1A2527),
    onSurfaceVariant = Color(0xFFA9BDB7),
    surfaceContainerLowest = Color(0xFF050809),
    surfaceContainerLow = Color(0xFF0C1214),
    surfaceContainer = Color(0xFF101719),
    surfaceContainerHigh = Color(0xFF162024),
    surfaceContainerHighest = Color(0xFF1C282C),
    outline = Color(0xFF3D514E),
    outlineVariant = Color(0xFF24342F),
    error = Color(0xFFE57373),
    onError = Color(0xFF2B0A0A),
    inverseSurface = Color(0xFFE2ECE8),
    inverseOnSurface = Color(0xFF101719),
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
