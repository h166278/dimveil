package com.h166278.dimveil.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DimVeilColors = darkColorScheme(
    primary = Color(0xFF8BE8C1),
    onPrimary = Color(0xFF082018),
    secondary = Color(0xFF82AFA0),
    background = Color(0xFF070B0D),
    surface = Color(0xFF101719),
    surfaceVariant = Color(0xFF1A2527),
    onBackground = Color(0xFFE2ECE8),
    onSurface = Color(0xFFE2ECE8),
    onSurfaceVariant = Color(0xFFA9BDB7),
    outline = Color(0xFF3D514E)
)

@Composable
fun DimVeilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DimVeilColors, content = content)
}
