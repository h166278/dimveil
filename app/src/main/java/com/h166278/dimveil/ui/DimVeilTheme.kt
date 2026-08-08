package com.h166278.dimveil.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Figma-inspired editor palette: neutral canvas, blue selection and compact corners.
private val Canvas = Color(0xFF202124)
private val Panel = Color(0xFF2C2D31)
private val PanelRaised = Color(0xFF37383D)
private val Stroke = Color(0xFF4A4B51)
private val Ink = Color(0xFFF5F5F6)
private val Muted = Color(0xFFB7B9C2)
private val Blue = Color(0xFF18A0FB)
private val Green = Color(0xFF55C2A5)
private val Red = Color(0xFFFF8A80)

private val DimVeilColors = darkColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF163A55),
    onPrimaryContainer = Color(0xFFD9F0FF),
    secondary = Green,
    onSecondary = Color(0xFF082A24),
    secondaryContainer = Color(0xFF203C37),
    onSecondaryContainer = Color(0xFFC5F5E8),
    background = Canvas,
    onBackground = Ink,
    surface = Panel,
    onSurface = Ink,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = Muted,
    surfaceContainerLowest = Canvas,
    surfaceContainerLow = Color(0xFF25262A),
    surfaceContainer = Panel,
    surfaceContainerHigh = PanelRaised,
    surfaceContainerHighest = PanelRaised,
    outline = Stroke,
    outlineVariant = Color(0xFF3A3B40),
    error = Red,
    onError = Color.White,
    inverseSurface = Ink,
    inverseOnSurface = Canvas,
    scrim = Canvas
)

private val DimVeilShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun DimVeilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DimVeilColors, shapes = DimVeilShapes, content = content)
}
