package com.h166278.dimveil.ui

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
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
        // 只旋转月牙本体，星点保持独立位置不变。
        group(rotate = 15f, pivotX = 12f, pivotY = 12f) {
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
            curveTo(20.8f, 12.5f, 20.8f, 13.7f, 20.3f, 14.8f)
        }
        }
        path(fill = SolidColor(androidx.compose.ui.graphics.Color.Black), pathFillType = PathFillType.NonZero) {
            // 星点保持原位。

            moveTo(18.4f, 5.0f)
            curveTo(18.8f, 6.0f, 19.4f, 6.6f, 20.4f, 7.0f)
            curveTo(19.4f, 7.4f, 18.8f, 8.0f, 18.4f, 9.0f)
            curveTo(18.0f, 8.0f, 17.4f, 7.4f, 16.4f, 7.0f)
            curveTo(17.4f, 6.6f, 18.0f, 6.0f, 18.4f, 5.0f)
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
            moveTo(3.5f, 6.7f)
            curveTo(6.6f, 5.2f, 9.5f, 5.6f, 12f, 7.5f)
            curveTo(14.5f, 5.6f, 17.4f, 5.2f, 20.5f, 6.7f)
            verticalLineTo(18.0f)
            curveTo(17.4f, 16.9f, 14.5f, 17.2f, 12f, 19.0f)
            curveTo(9.5f, 17.2f, 6.6f, 16.9f, 3.5f, 18.0f)
            close()
            moveTo(12f, 7.5f)
            verticalLineTo(19.0f)
            moveTo(16.0f, 5.9f)
            verticalLineTo(10.4f)
            lineTo(17.7f, 9.3f)
            lineTo(19.2f, 10.4f)
            verticalLineTo(6.1f)
        }
    }.build()
}
