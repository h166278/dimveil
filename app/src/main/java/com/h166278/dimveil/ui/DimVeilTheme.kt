package com.h166278.dimveil.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

private val DimVeilTypography = androidx.compose.material3.Typography(
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp)
)

@Composable
fun DimVeilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DimVeilColors, typography = DimVeilTypography, content = content)
}
