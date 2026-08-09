package com.h166278.dimveil.ui

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val NightModeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "NightMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(20.3f, 14.8f)
            curveTo(18.2f, 18.8f, 13.3f, 20.3f, 9.3f, 18.2f)
            curveTo(5.3f, 16.1f, 3.8f, 11.2f, 5.9f, 7.2f)
            curveTo(7.1f, 4.9f, 9.3f, 3.4f, 11.8f, 3.1f)
            curveTo(10.8f, 6.7f, 12.7f, 10.5f, 16.3f, 11.5f)
            curveTo(17.6f, 11.9f, 19.1f, 11.9f, 20.3f, 11.4f)
        }
        path(fill = SolidColor(androidx.compose.ui.graphics.Color.Black), pathFillType = PathFillType.NonZero) {
            moveTo(17.7f, 4.2f)
            curveTo(18.1f, 5.2f, 18.7f, 5.8f, 19.7f, 6.2f)
            curveTo(18.7f, 6.6f, 18.1f, 7.2f, 17.7f, 8.2f)
            curveTo(17.3f, 7.2f, 16.7f, 6.6f, 15.7f, 6.2f)
            curveTo(16.7f, 5.8f, 17.3f, 5.2f, 17.7f, 4.2f)
        }
    }.build()
}

internal val ReadingModeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ReadingMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4.2f, 5.8f)
            curveTo(6.9f, 4.5f, 9.5f, 5.0f, 12f, 7.1f)
            curveTo(14.5f, 5.0f, 17.1f, 4.5f, 19.8f, 5.8f)
            verticalLineTo(18.7f)
            curveTo(17.0f, 17.4f, 14.4f, 17.9f, 12f, 20f)
            curveTo(9.6f, 17.9f, 7.0f, 17.4f, 4.2f, 18.7f)
            close()
            moveTo(12f, 7.1f)
            verticalLineTo(20f)
            moveTo(16.2f, 5.4f)
            verticalLineTo(10.1f)
            lineTo(17.8f, 9.0f)
            lineTo(19.2f, 10.1f)
            verticalLineTo(5.6f)
        }
    }.build()
}
